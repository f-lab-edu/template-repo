package io.github.jaehyeonhan.project.service;

import io.github.jaehyeonhan.project.entity.Participation;
import io.github.jaehyeonhan.project.entity.ParticipationCache;
import io.github.jaehyeonhan.project.entity.ParticipationRole;
import io.github.jaehyeonhan.project.repository.ParticipationRepository;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.Optional;

import static io.github.jaehyeonhan.project.service.ChatTestConst.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class ParticipationCacheResilienceIntegrationTest {

    @Autowired
    ParticipationCacheService participationCacheService;

    @Autowired
    CircuitBreakerRegistry circuitBreakerRegistry;

    @MockitoBean
    ParticipationRepository participationRepository;

    @MockitoBean
    RedisTemplate<String, ParticipationCache> participationRedisTemplate;

    @MockitoBean
    ValueOperations<String, ParticipationCache> valueOperations;

    @BeforeEach
    void setUp() {
        given(participationRedisTemplate.opsForValue())
            .willReturn(valueOperations);

        circuitBreakerRegistry
            .circuitBreaker("participationCache")
            .reset();
    }

    @Test
    @DisplayName("Redis 일시 실패 시 retry 후 성공한다.")
    void given_temporaryRedisFailure_when_get_then_retryAndSucceed() {

        // given
        String key = "app:participation:" + USER_ID + ":" + CHAT_ID;

        ParticipationCache cached = new ParticipationCache(
            PARTICIPATION_ID,
            ParticipationRole.USER,
            LocalDateTime.now()
        );

        given(valueOperations.get(key))
            .willThrow(new RedisConnectionFailureException("redis fail"))
            .willThrow(new RedisConnectionFailureException("redis fail"))
            .willReturn(cached);

        // when
        ParticipationCache result = participationCacheService.get(USER_ID, CHAT_ID);

        // then
        then(valueOperations)
            .should(times(3))
            .get(key);

        assertThat(result).isEqualTo(cached);
    }

    @Test
    @DisplayName("Redis 실패가 반복되면 CircuitBreaker가 OPEN 된다.")
    void given_repeatedRedisFailure_when_get_then_openCircuitBreaker() {

        // given
        String key = "app:participation:" + USER_ID + ":" + CHAT_ID;

        given(valueOperations.get(key))
            .willThrow(new RedisConnectionFailureException("redis failure"));

        given(participationRepository.findByUserIdAndChatId(USER_ID, CHAT_ID))
            .willReturn(Optional.empty());

        CircuitBreaker circuitBreaker =
            circuitBreakerRegistry.circuitBreaker("participationCache");

        // when
        for (int i = 0; i < 10; i++) {
            participationCacheService.get(USER_ID, CHAT_ID);
        }

        // then
        assertThat(circuitBreaker.getState())
            .isEqualTo(CircuitBreaker.State.OPEN);
    }

    @Test
    @DisplayName("CircuitBreaker OPEN 상태에서는 Redis를 호출하지 않고 fallback DB 조회만 수행한다.")
    void given_circuitBreakerOpen_when_get_then_skipRedisAndQueryDbFallback() {

        // given
        CircuitBreaker circuitBreaker =
            circuitBreakerRegistry.circuitBreaker("participationCache");

        circuitBreaker.transitionToOpenState();

        Participation participation = Participation.joinAsUser(
            PARTICIPATION_ID,
            USER_ID,
            CHAT_ID
        );

        given(participationRepository.findByUserIdAndChatId(USER_ID, CHAT_ID))
            .willReturn(Optional.of(participation));

        // when
        ParticipationCache result = participationCacheService.get(USER_ID, CHAT_ID);

        // then
        then(valueOperations)
            .shouldHaveNoInteractions();

        then(participationRepository)
            .should(times(1))
            .findByUserIdAndChatId(USER_ID, CHAT_ID);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(PARTICIPATION_ID);
    }
}
package io.github.jaehyeonhan.project.service;

import io.github.jaehyeonhan.project.entity.Participation;
import io.github.jaehyeonhan.project.entity.ParticipationCache;
import io.github.jaehyeonhan.project.entity.ParticipationRole;
import io.github.jaehyeonhan.project.repository.ParticipationRepository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import static io.github.jaehyeonhan.project.service.ChatTestConst.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class ParticipationCacheTest {

    @Mock
    private ParticipationRepository participationRepository;

    @Mock
    private RedisTemplate<String, ParticipationCache> participationRedisTemplate;

    @Mock
    private ValueOperations<String, ParticipationCache> valueOperations;

    private ParticipationCacheService participationCacheService;

    @BeforeEach
    void setUp() {
        participationCacheService = new ParticipationCacheService(
            participationRepository, participationRedisTemplate, new SimpleMeterRegistry());
        given(participationRedisTemplate.opsForValue()).willReturn(valueOperations);
    }

    @Test
    @DisplayName("캐시 미스 시 DB 조회 후 Redis에 저장한다.")
    void given_cacheMiss_when_get_then_queryDbAndCacheResult() {

        // given
        String key = "app:participation:" + USER_ID + ":" + CHAT_ID;

        Participation participation = Participation.joinAsUser(PARTICIPATION_ID, USER_ID, CHAT_ID);

        given(valueOperations.get(key)).willReturn(null);

        given(participationRepository.findByUserIdAndChatId(USER_ID, CHAT_ID)).willReturn(
            Optional.of(participation));

        // when
        ParticipationCache result = participationCacheService.get(USER_ID, CHAT_ID);

        // then
        then(participationRepository).should(times(1)).findByUserIdAndChatId(USER_ID, CHAT_ID);

        then(valueOperations).should(times(1))
            .set(eq(key), any(ParticipationCache.class), eq(Duration.ofMinutes(5)));

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(PARTICIPATION_ID);
        assertThat(result.role()).isEqualTo(ParticipationRole.USER);
    }

    @Test
    @DisplayName("캐시 히트 시 DB를 조회하지 않는다.")
    void given_cacheHit_when_get_then_returnCachedValueWithoutDbQuery() {

        // given
        String key = "app:participation:" + USER_ID + ":" + CHAT_ID;

        ParticipationCache cached = new ParticipationCache(
            PARTICIPATION_ID,
            ParticipationRole.USER,
            LocalDateTime.now()
        );

        given(valueOperations.get(key)).willReturn(cached);

        // when
        ParticipationCache result = participationCacheService.get(USER_ID, CHAT_ID);

        // then
        then(participationRepository).shouldHaveNoInteractions();

        then(valueOperations).should(never()).set(anyString(), any(), any(Duration.class));

        assertThat(result).isEqualTo(cached);
    }

    @Test
    @DisplayName("DB 조회 결과가 없으면 Redis에 저장하지 않는다.")
    void given_dbReturnsNull_when_get_then_doNotCacheNullValue() {

        // given
        String key = "app:participation:" + USER_ID + ":" + CHAT_ID;

        given(valueOperations.get(key)).willReturn(null);

        given(participationRepository.findByUserIdAndChatId(USER_ID, CHAT_ID)).willReturn(
            Optional.empty());

        // when
        ParticipationCache result = participationCacheService.get(USER_ID, CHAT_ID);

        // then
        then(participationRepository).should(times(1)).findByUserIdAndChatId(USER_ID, CHAT_ID);

        then(valueOperations).should(never()).set(anyString(), any(), any(Duration.class));

        assertThat(result).isNull();
    }
}
package io.github.jaehyeonhan.project.service;

import static io.github.jaehyeonhan.project.service.ChatTestConst.CHAT_ID;
import static io.github.jaehyeonhan.project.service.ChatTestConst.MESSAGE_ID;
import static io.github.jaehyeonhan.project.service.ChatTestConst.PARTICIPATION_ID;
import static io.github.jaehyeonhan.project.service.ChatTestConst.USER_ID;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import io.github.jaehyeonhan.project.entity.Participation;
import io.github.jaehyeonhan.project.entity.ParticipationCache;
import io.github.jaehyeonhan.project.repository.MessageRepository;
import io.github.jaehyeonhan.project.repository.ParticipationRepository;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class SendMessageBulkheadIntegrationTest {

    @Autowired
    ChatService chatService;

    @MockitoBean
    ChatValidationService chatValidationService;

    @MockitoBean
    MessageRepository messageRepository;

    @MockitoBean
    IdGenerator idGenerator;

    @MockitoBean
    ParticipationRepository participationRepository;

    @MockitoBean
    RedisTemplate<String, ParticipationCache> participationRedisTemplate;

    @Test
    @DisplayName("sendMessage() Bulkhead 동시 호출 제한을 초과하면 BulkheadFullException이 발생한다.")
    void given_bulkheadLimitExceeded_when_sendMessage_then_throwBulkheadFullException()
        throws Exception {

        Participation participation = Participation.joinAsUser(PARTICIPATION_ID, USER_ID, CHAT_ID);

        CountDownLatch requestsStarted = new CountDownLatch(2);
        CountDownLatch releaseRequests = new CountDownLatch(1);

        given(chatValidationService.requireParticipation(USER_ID, CHAT_ID))
            .willAnswer(invocation -> {
                requestsStarted.countDown();
                releaseRequests.await(3, TimeUnit.SECONDS);
                return participation;
            });

        given(idGenerator.generate()).willReturn(MESSAGE_ID);

        try (ExecutorService executor = Executors.newFixedThreadPool(4)) {

            // 2개의 요청이 Bulkhead semaphore를 점유 (max-concurrent-calls: 2)
            Future<?> first = executor.submit(() ->
                chatService.sendMessage(USER_ID, CHAT_ID, "msg"));
            Future<?> second = executor.submit(() ->
                chatService.sendMessage(USER_ID, CHAT_ID, "msg"));

            // 두 요청이 모두 Bulkhead 내부에서 블로킹될 때까지 대기
            requestsStarted.await();

            // 3번째 요청은 max-concurrent-calls: 2 초과 → 즉시 거부
            Future<?> third = executor.submit(() ->
                chatService.sendMessage(USER_ID, CHAT_ID, "msg"));

            assertThatThrownBy(third::get)
                .hasCauseInstanceOf(BulkheadFullException.class);

            releaseRequests.countDown();

            first.get();
            second.get();

            executor.shutdownNow();
        }
    }
}

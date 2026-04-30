package io.github.jaehyeonhan.project.service;

import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.bulkhead.annotation.Bulkhead.Type;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.jaehyeonhan.project.entity.ParticipationCache;
import io.github.jaehyeonhan.project.repository.ParticipationRepository;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ParticipationCacheService {

    private static final String KEY_PREFIX = "app:participation:";
    private static final Duration TTL = Duration.ofMinutes(5); // TTL은 최악의 상황에서 stale cache를 허용할 수 있는 최대

    private final ParticipationRepository participationRepository;
    private final RedisTemplate<String, ParticipationCache> participationRedisTemplate;

    @CircuitBreaker(name = "participationCache", fallbackMethod = "getFallback")
    @Retry(name = "participationCache")
    public ParticipationCache get(String userId, String chatId) {
        String key = KEY_PREFIX + userId + ":" + chatId;
        ParticipationCache cached = participationRedisTemplate.opsForValue().get(key);
        if (cached != null) {
            return cached;
        }
        ParticipationCache result = queryDb(userId, chatId);
        if (result != null) {
            participationRedisTemplate.opsForValue().set(key, result, TTL);
        }
        return result;
    }

    // Circuit Open — Redis를 건너뛰고 DB 직접 조회
    private ParticipationCache getFallback(String userId, String chatId, Throwable t) {
        log.warn("participationCache fallback: cause={}", t.getMessage());
        return queryDb(userId, chatId);
    }

    private ParticipationCache queryDb(String userId, String chatId) {
        return participationRepository.findByUserIdAndChatId(userId, chatId)
            .map(p -> new ParticipationCache(p.getId(), p.getRole(), p.getCreatedAt()))
            .orElse(null);
    }
}

# Redis Participation Cache 계획

## 목적

`sendMessage`에서 `chatValidationService.requireParticipation(userId, chatId)` 호출 시 매번
`SELECT ... FROM participation WHERE user_id = ? AND chat_id = ?` DB 쿼리가 발생한다.
메시지 전송은 같은 사용자가 동일 채팅에서 반복 호출하는 가장 빈번한 연산이므로,
Participation 조회 결과를 Redis에 캐시해 DB 부하를 줄인다.

---

## 1. 캐시 키

```
participation::{userId}:{chatId}
```

Spring Cache SpEL: `@Cacheable(cacheNames = "participation", key = "#userId + ':' + #chatId")`

예시: `participation::user-uuid-123:chat-uuid-456`

`userId + chatId` 조합이 Participation을 유일하게 특정하는 자연키이므로 별도 해시 불필요.

---

## 2. TTL

**30분 (1800초)**

- Participation은 삭제·변경 이벤트가 거의 없다(현재 코드에 leave·kick 없음).
- 30분: 라이브 세션 재접속·역할 변경 등 예외 상황에서 stale 상태가 지속되는 최대 허용 시간.
- 너무 길면(>1h) 역할 변경 시 stale data 위험; 너무 짧으면(<5m) cache miss 비율이 높아 효과 감소.

---

## 3. 변경 지점과 변경 이유

### 3-1. `app/build.gradle` — 의존성 추가

```groovy
implementation 'org.springframework.boot:spring-boot-starter-data-redis'
```

**이유**: Lettuce(기본 Redis 클라이언트) + `spring-cache` Redis 통합 제공. Sentinel 연결도 자동 지원.

---

### 3-2. `app/src/main/resources/application.yml` — Redis 연결 설정

```yaml
spring:
  data:
    redis:
      sentinel:
        master: mymaster
        nodes:
          - localhost:26379
          - localhost:26380
          - localhost:26381
```

**이유**: `infra/docker-compose.yml`에 이미 Sentinel 3노드(26379~26381)가 구성되어 있으므로
단순 단일 노드 대신 Sentinel 연결을 사용한다.

---

### 3-3. `config/CacheConfig.java` (신규) — CacheManager 설정

```java
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory factory) {
        ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .activateDefaultTyping(
                BasicPolymorphicTypeValidator.builder().allowIfBaseType(Object.class).build(),
                ObjectMapper.DefaultTyping.NON_FINAL
            );

        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(30))
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    new GenericJackson2JsonRedisSerializer(mapper)
                )
            );

        return RedisCacheManager.builder(factory)
            .cacheDefaults(config)
            .build();
    }
}
```

**이유**: 기본 `ConcurrentMapCacheManager`는 JVM 메모리 내에만 저장되며 TTL이 없다.
`RedisCacheManager`로 교체해야 Redis에 저장되고 TTL이 적용된다.
`GenericJackson2JsonRedisSerializer`는 타입 정보를 JSON에 포함하므로 역직렬화 시 명시적 타입 지정이 불필요.

---

### 3-4. `entity/Participation.java` — `@JsonCreator` 역직렬화 팩토리 추가

```java
@JsonCreator
static Participation jsonDeserialize(
    @JsonProperty("id") String id,
    @JsonProperty("userId") String userId,
    @JsonProperty("chatId") String chatId,
    @JsonProperty("role") ParticipationRole role,
    @JsonProperty("createdAt") LocalDateTime createdAt) {
    Participation p = new Participation();  // protected 기본 생성자
    p.id = id;
    p.userId = userId;
    p.chatId = chatId;
    p.role = role;
    p.createdAt = createdAt;
    return p;
}
```

**이유**: `Participation`의 생성자는 `private`이고 `@NoArgsConstructor(access = PROTECTED)`라
Jackson이 기본 설정으로는 인스턴스를 생성할 수 없다.

`ObjectMapper.setVisibility(FIELD, ANY)`로 우회할 수도 있지만, 이는 해당 ObjectMapper로
캐시되는 **모든 타입**에 영향을 미치는 전역 설정이다. `@JsonCreator`를 명시하면 `Participation`이
자신의 역직렬화 방법을 직접 선언하므로, 범위가 타입 단위로 한정되고 의도가 코드에 드러난다.

같은 클래스 내의 메서드이므로 `protected` 기본 생성자와 `private` 필드에 직접 접근할 수 있다.
직렬화(Java → JSON)는 `@Getter`가 생성하는 public getter로 이미 동작한다.

---

### 3-5. `ChatValidationService.requireParticipation()` — `@Cacheable` 추가

```java
@Cacheable(cacheNames = "participation", key = "#userId + ':' + #chatId")
public Participation requireParticipation(String userId, String chatId) {
    return participationRepository.findByUserIdAndChatId(userId, chatId)
        .orElseThrow(() -> new NotParticipatingException("참여 중인 채팅이 아닙니다."));
}
```

**이유**: 이 메서드는 `sendMessage`, `getMessageList`, `blockUser`, `unblockUser` 모두에서 호출된다.
`sendMessage`만 아니라 전체에 캐시 효과가 적용되며, 로직 중복 없이 한 곳에서 관리할 수 있다.

---

## 4. 실행 단계

| 순서 | 작업 | 파일 |
|---|---|---|
| 1 | `spring-boot-starter-data-redis` 의존성 추가 | `app/build.gradle` |
| 2 | Redis Sentinel 연결 설정 추가 | `app/src/main/resources/application.yml` |
| 3 | `CacheConfig.java` 생성: `@EnableCaching`, `RedisCacheManager`, TTL 30분 | `app/src/main/java/.../config/CacheConfig.java` |
| 4 | `Participation`에 `@JsonCreator` 역직렬화 팩토리 추가 | `entity/Participation.java` |
| 5 | `ChatValidationService.requireParticipation()`에 `@Cacheable` 추가 | `ChatValidationService.java` |
| 6 | 테스트: `sendMessage` 두 번 연속 호출 시 `participationRepository.findByUserIdAndChatId`가 1회만 실행되는지 `verify(repo, times(1))` 검증 | `ChatServiceIntegrationTest.java` |
| 7 | Redis 없는 환경(로컬 단위 테스트)에서 캐시가 비활성화되는지 확인 (`@ActiveProfiles("test")` + `@TestPropertySource`로 `cache.type=none` 설정) | 테스트 설정 |

---

## 주의사항

- `requireParticipation()`은 `@Transactional` 메서드 내부에서도 호출된다(`blockUser`, `unblockUser`).
  Spring Cache는 트랜잭션과 독립적으로 동작하므로, 트랜잭션 롤백이 발생해도 캐시는 evict되지 않는다.
  현재 코드에서 `requireParticipation()`은 읽기 전용 조회이므로 롤백 시 캐시 불일치가 생길 여지는 없다.
- `Participation`의 `createdAt`은 생성자에서 `LocalDateTime.now()`로 설정되며,
  캐시 복원 시 이 값이 그대로 재사용된다. 시간 기반 로직에 `createdAt`을 쓰지 않으므로 문제없다.

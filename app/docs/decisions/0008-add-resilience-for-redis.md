# Add Bulkhead, Circuit Breaker, and Retry for Redis Cache

## Context and Problem Statement

The project utilizes Redis to reduce the usage of Database connections in send message API. Even though Redis is highly available through Sentinels, timeouts may occur during Sentinel fail-over resulting in significant queuing in the API, eventually leading to considerably increased latency.

## Major Design Decisions

Three resilience features are implemented.

1. Circuit Breaker: Use DB fallback when Redis cache is not available or under significant degradation.
2. Retry: Absorb transient delays or errors with Redis operations.
3. Bulkhead: When circuit breaker opens, prevent database connection exhaustion, as well as in normal times.

The actual call order is: Bulkhead → Circuit Breaker → Retry → Redis.
When the circuit breaker is OPEN, the request immediately falls back to the database without executing retries.

### Consequences

* Good, because Redis fail-over no longer causes excessive API queuing and severe cascading latency under transient failures.
* Good, because requests can continue through database fallback even when Redis becomes unavailable or significantly degraded.
* Good, because transient Redis failures can be absorbed through retries, improving short-term request success rates.
* Bad, because retry attempts may increase overall request latency before fallback or failure occurs.
* Bad, because Bulkhead limits may reduce throughput even during normal Redis operation.
* Bad, because the interaction between Bulkhead, Circuit Breaker, Retry, and timeout configurations increases operational and tuning complexity.

### Considerations
* Server could return 429 Too Many Request response when the server exceeds Bulkhead limits. It might be considered in the 
* Since the resilience configurations were not validated against production-scale traffic, optimal values for Circuit Breaker thresholds, Retry counts, and timeout durations could not be fully determined.
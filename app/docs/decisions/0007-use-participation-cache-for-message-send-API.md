# Participation Lookup Caching for Message Send API

## Context and Problem Statement

The message sending API performs a `(userId, chatId) → participationId` lookup on every request. Under increased traffic, this results in a linear increase in database read load.

In addition, message sending requires a block validation check to ensure that blocked users cannot send messages. This introduces an additional database read on the hot path, further increasing read pressure.

This raises concerns about scalability of the DB read path under high traffic.

## Considered Options

- Keep DB-only architecture (no caching)
- Denormalize participation data (include block status in participation)
- Apply Redis-based cache-aside pattern for participation lookup

## Decision Outcome

**Chosen option**: Redis-based cache-aside pattern for participation lookup

**Reasons**

- DB-only architecture results in linear growth of read load as traffic increases.
- Denormalization approach was rejected due to consistency requirements. Block status must be reflected immediately, and relying on cache invalidation introduces risk of stale state due to potential invalidation failure. Additionally, reducing TTL to mitigate staleness significantly reduces cache effectiveness under expected traffic patterns. Block validation is intentionally excluded for the same reason.
- Redis cache-aside approach reduces DB load by caching `(userId, chatId) → participationId` mappings and leverages locality in chat usage patterns. Given the nature of chat systems, repeated message sending within the same chat context is expected.

### Considerations

- Participation cache is applied only to lookup path and must not be used for authorization decisions.
- Block validation remains fully DB-driven to ensure correctness and avoid stale state issues.
- Cache invalidation strategy is optional and TTL-based caching is acceptable as there is no leave operation, and the mutation frequency of participation mappings is expected to be low in the future as well.
- System still requires DB as the source of truth for all authorization-related logic.
- Cache effectiveness depends on actual traffic locality characteristics and may vary under different usage patterns.
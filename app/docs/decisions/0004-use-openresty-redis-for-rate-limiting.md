# Use OpenResty and Redis for Rate Limiting

## Context and Problem Statement

We needed to apply a consistent rate-limiting policy across the entire service.

**Requirements**
- Fast enough that it doesn't increase the response time significantly.
- Support multiple rate limiting clients. All clients should apply the same rate limiting policy. Temporary inconsistency less than one second is allowed, but have to put it to the minimum.

## Considered Options

* Implement rate limiting with Nginx (OpenResty) + Lua + Redis
* Implement rate limiting inside the application server (e.g. logic in service layer, Bucket4j)
* Use Nginx’s built-in features such as `limit_req` without Redis

## Decision Outcome

**Chosen option**: Nginx(OpenResty) + Redis with Lua scripting

**Reasons**
- It blocks excessive traffic at the proxy layer before reaching the application. In the current set up, application doesn't have to know about if user is rate-limited or not.
- Redis provides a centralized, consistent state across multiple clients, and is fast.

### Consequences

- **Using Lua Scripts**  
  The token bucket algorithm requires atomic updates of both the token count and the last refill timestamp. Lua scripts allow these updates to be executed atomically within Redis, ensuring data consistency.

- **Dependence on Redis**
  If Redis becomes unavailable, rate-limiting behavior may degrade and fallback strategies are required. High availability options for Redis such as Sentinel and Cluster is required.
  
- **Fallback Rate Limiting with user_id Header by Default Nginx Rate Limiting**
  When Redis is completely unavailable, Nginx rate limits by its embedded rate limiting mechanism with user-id header.
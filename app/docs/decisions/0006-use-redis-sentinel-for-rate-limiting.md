# Use Redis Sentinel for Rate Limiting for High Availability

## Context and Problem Statement

When a single Redis instance fails, the system might have to stop the rate limiting indefinitely. The rate limiting system should be more resilient to Redis failures. 

## Considered Options

- Redis Sentinel
- Redis Cluster
- Cloud managed Redis

## Decision Outcome

**Chosen option**: Redis Sentinel

**Reasons**

- Cluster is harder to manage due to its constraints in key distribution. Yet, it is not clear that write load is significantly intense to shard. And it needs more instances.
- Cloud service is not affordable for the current budget.


### Considerations

- Still needs a fallback strategy in Nginx in case the entire Redis instances or Sentinels fail.
- Need to implement Sentinel-aware logic in Nginx.
- Need more nodes to set up Sentinels.
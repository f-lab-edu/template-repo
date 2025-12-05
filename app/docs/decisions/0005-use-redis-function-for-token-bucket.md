# Use Redis Function for Token Bucket Algorithm Implementation

## Context and Problem Statement

If Nginx instances manage the lua files of token bucket algorithm, they use `EVAL` to execute the script. However, `EVAL` sends the whole script as a string increasing the network cost.

We should find a way to efficiently execute the token bucket algorithm in Redis.

## Considered Options

* `SCRIPT LOAD` in Redis and `EVALSHA` in Nginx
* Redis Functions

## Decision Outcome

**Chosen option**: Redis Functions

**Reasons**

- When using `EVALSHA`, all Nginx instances must share the same sha to prevent inconsistency of rate limiting policy. Also, when the script changes, all Nginx instances should call `SCRIPT LOAD`, which complicates the Nginx settings and distribution strategy.
- Redis functions have persistence options and are automatically copied to replicas.

### Considerations

- Logic debugging becomes harder since the code is inside Redis itself.
- Rollback of function versions requires careful handling.
- Version control of the token bucket algorithm implementation should be handled in distribution server.
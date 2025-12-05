# Use Token Bucket Algorithm for Rate Limiting

## Context and Problem Statement

Chat system needs rate limiting for each user's sending messages. There are five well-known algorithms.

Which algorithm is the most appropriate for the this chat system?

## Considered Options

- Leaky Bucket
- Fixed Window Counter
- Sliding Window Log
- Sliding Window Counter
- Token Bucket

## Decision Outcome

**Chosen option**: Token Bucket

**Background**:

When choosing a rate limiting algorithm, we must consider how precise rate should be and whether to allow bursts.

In case of chat, the approximate rate limiting is acceptable, and it should allow bursts as users often send multiple short messages.

**Reasons**:

1. Leaky Bucket: Flow control to backend servers for normal requests is not ideal. Message should be sent immediately if server is available.
2. Fixed Window Counter: The big burst at the edge of the windows is not acceptable.
3. Sliding Window Log & Sliding Window Counter: The cost of memory usage is bigger than benefit of the accuracy of limiting rate. And it is more difficult to implement.

### Consequences

- Requires centralized storage for tokens to enforce rate limits across distributed clients.
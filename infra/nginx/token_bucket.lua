local bucket_size = tonumber(redis.call("GET", "rate_limit:config:bucket_size"))
local refill_rate = tonumber(redis.call("GET", "rate_limit:config:refill_rate"))

local user_id = ARGV[1]
local t = redis.call("TIME")
local now_sec = tonumber(t[1])

-- 현재 토큰 개수 조회 및 token 변수 갱신
local tokens_key = "rate_limit:user:" .. user_id .. ":tokens"
local tokens = tonumber(redis.call("GET", tokens_key))
if tokens == nil then tokens = bucket_size end

-- 마지막 충전 시각 조회
local ts_key = "rate_limit:user:" .. user_id .. ":last_refill"
local last = tonumber(redis.call("GET", ts_key))
if not last then 
    last = now_sec 
    redis.call("SET", ts_key, now_sec)
end

-- 시간이 충분히 지났다면 토큰 추가
local elapsed = now_sec - last
local to_fill = math.floor(elapsed * refill_rate)

local filled = to_fill > 0
if filled then redis.call("SET", ts_key, now_sec) end

tokens = math.min(bucket_size, tokens + to_fill)

-- 토큰 사용
local allowed = 0
if tokens >= 1 then
  allowed = 1
  tokens = tokens - 1
end

-- Redis에 토큰 개수 저장
redis.call("SET", tokens_key, tokens)


return allowed
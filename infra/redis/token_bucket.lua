#!lua name=token_bucket

local function check_if_allowed(keys, args)
    local bucket_size = tonumber(redis.call("GET", "rate_limit:config:bucket_size")) -- cluster 적용 시 args로 받아야 함
    local refill_rate = tonumber(redis.call("GET", "rate_limit:config:refill_rate"))  -- cluster 적용 시 args로 받아야 함

    local user_id = args[1] -- cluster 적용 시 keys로 변경해야 함
    local t = redis.call("TIME")
    local now_sec = tonumber(t[1])

    -- 현재 토큰 개수 조회 및 token 변수 갱신
    local tokens_key = "rate_limit:user:" .. user_id .. ":tokens" -- cluster 적용 시 hashtag 적용 필요
    local tokens = tonumber(redis.call("GET", tokens_key))
    if tokens == nil then tokens = bucket_size end

    -- 마지막 충전 시각 조회
    local ts_key = "rate_limit:user:" .. user_id .. ":last_refill" -- cluster 적용 시 hashtag 적용 필요
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
end

redis.register_function('check_if_allowed', check_if_allowed)
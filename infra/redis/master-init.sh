cat /etc/redis/lua/token_bucket.lua | redis-cli -x FUNCTION LOAD
redis-cli SET rate_limit:config:bucket_size 4
redis-cli SET rate_limit:config:refill_rate 1
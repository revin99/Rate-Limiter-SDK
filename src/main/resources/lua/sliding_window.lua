-- KEYS[1] -> sliding window key
-- ARGV[1] -> limit
-- ARGV[2] -> windowSeconds
-- ARGV[3] -> currentTimeMillis

local key = KEYS[1]
local limit = tonumber(ARGV[1])
local windowMillis = tonumber(ARGV[2]) * 1000
local now = tonumber(ARGV[3])

-- remove expired requests
redis.call("ZREMRANGEBYSCORE", key, 0, now - windowMillis)

local count = redis.call("ZCARD", key)

-- reject case
if count >= limit then
    local oldest = redis.call("ZRANGE", key, 0, 0, "WITHSCORES")[2]
    local retryAfterMillis = (oldest + windowMillis) - now
    local retryAfterSeconds = math.ceil(retryAfterMillis / 1000)

    return {0, count, retryAfterSeconds}
end

-- allow request
redis.call("ZADD", key, now, tostring(now))
redis.call("EXPIRE", key, ARGV[2] * 2)

return {1, count + 1, 0}

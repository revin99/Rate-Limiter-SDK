-- KEYS[1] -> token bucket key
-- ARGV[1] -> capacity
-- ARGV[2] -> refillSeconds
-- ARGV[3] -> currentTimeMillis

local capacity = tonumber(ARGV[1])
local refillSeconds = tonumber(ARGV[2])
local now = tonumber(ARGV[3])

-- read current state
local tokens = tonumber(redis.call("GET", KEYS[1] .. ":tokens"))
local lastRefill = tonumber(redis.call("GET", KEYS[1] .. ":ts"))

-- first request case
if tokens == nil or lastRefill == nil then
    tokens = capacity
    lastRefill = now
end

-- calculate refill
local elapsedMillis = now - lastRefill
local refillTokens = math.floor((elapsedMillis * capacity) / (refillSeconds * 1000))

if refillTokens > 0 then
    tokens = math.min(capacity, tokens + refillTokens)
    lastRefill = now
end

-- consume token if possible
if tokens <= 0 then
    return -1
end

tokens = tokens - 1

-- persist state
redis.call("SET", KEYS[1] .. ":tokens", tokens)
redis.call("SET", KEYS[1] .. ":ts", lastRefill)



redis.call("EXPIRE", KEYS[1] .. ":tokens", refillSeconds *2)
redis.call("EXPIRE", KEYS[1] .. ":ts", refillSeconds * 2)

-- return remaining tokens
return tokens

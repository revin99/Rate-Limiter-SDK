-- KEYS[1] -> leaky bucket key
-- ARGV[1] -> capacity
-- ARGV[2] -> leakSeconds (time to fully drain bucket)
-- ARGV[3] -> currentTimeMillis

local capacity = tonumber(ARGV[1])
local leakSeconds = tonumber(ARGV[2])
local now = tonumber(ARGV[3])

local levelKey = KEYS[1] .. ":level"
local tsKey = KEYS[1] .. ":ts"

-- fetch state
local level = tonumber(redis.call("GET", levelKey))
local lastTs = tonumber(redis.call("GET", tsKey))

-- first request
if level == nil or lastTs == nil then
    level = 0
    lastTs = now
end

-- leak calculation
local elapsedMillis = now - lastTs
local leakRatePerMs = capacity / (leakSeconds * 1000)
local leaked = math.floor(elapsedMillis * leakRatePerMs)

if leaked > 0 then
    level = math.max(0, level - leaked)
    lastTs = now
end

-- bucket full → reject
if level >= capacity then
    return {0, level}
end

-- accept request
level = level + 1

-- persist state
redis.call("SET", levelKey, level)
redis.call("SET", tsKey, lastTs)

-- cleanup
redis.call("EXPIRE", levelKey, leakSeconds * 2)
redis.call("EXPIRE", tsKey, leakSeconds * 2)

return {1, level}

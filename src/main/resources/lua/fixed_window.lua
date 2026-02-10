-- KEYS[1] -> fixed window key
-- ARGV[1] -> limit
-- ARGV[2] -> windowSeconds

local limit = tonumber(ARGV[1])
local windowSeconds = tonumber(ARGV[2])

-- increment counter
local count = redis.call("INCR", KEYS[1])

-- first request → start window
if count == 1 then
    redis.call("EXPIRE", KEYS[1], windowSeconds)
end

-- over limit
if count > limit then
    return -1
end

-- allowed → return current count
return count

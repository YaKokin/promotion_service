local key = KEYS[1]
local currValue = redis.call('GET', key)

if not currValue or tonumber(currValue) <= 0 then
    return 0
else
    local newValue = redis.call('DECR', key)
    if newValue <= 0 then
        redis.call('DEL', key)
    end
    return newValue
end
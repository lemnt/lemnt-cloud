-- mall:settlement:bind
if redis.call('EXISTS', KEYS[2]) == 1 then return 0 end
local previous = redis.call('GET', KEYS[1])
if previous then
    if previous == ARGV[1] then return 2 end
    return 0
end
redis.call('SET', KEYS[1], ARGV[1], 'EX', ARGV[2])
return 1

-- mall:settlement:save
local order = ARGV[2]
local previous = redis.call('GET', KEYS[2])
if previous and previous ~= order then return 0 end
local snapshot = redis.call('GET', KEYS[1])
if snapshot then
    if snapshot ~= ARGV[1] then return 0 end
    if previous == order then return 2 end
end
redis.call('SET', KEYS[1], ARGV[1], 'EX', ARGV[3])
redis.call('SET', KEYS[2], order, 'EX', ARGV[3])
for i = 3, #KEYS do
    redis.call('SADD', KEYS[i], order)
    redis.call('EXPIRE', KEYS[i], ARGV[3])
end
return 1

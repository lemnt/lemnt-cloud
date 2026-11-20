-- mall:settlement:rollback
if redis.call('GET', KEYS[1]) ~= ARGV[1] then return 0 end
redis.call('DEL', KEYS[1])
if redis.call('GET', KEYS[2]) == ARGV[2] then redis.call('DEL', KEYS[2]) end
for i = 3, #KEYS do redis.call('SREM', KEYS[i], ARGV[2]) end
return 1

-- mall:settlement:abandon
if redis.call('EXISTS', KEYS[2]) == 1 then return 0 end
if redis.call('EXISTS', KEYS[1]) == 1 then return 2 end
redis.call('SET', KEYS[1], '1', 'EX', ARGV[1])
return 1

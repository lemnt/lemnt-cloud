package com.lemnt.mall.start;

import com.baomidou.lock.LockTemplate;
import com.baomidou.lock.executor.RedissonLockExecutor;
import com.baomidou.lock.spring.boot.autoconfigure.Lock4jProperties;
import org.redisson.api.RLock;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

/** 仅替代 Redis 传输；保留真实 LockTemplate、事务同步和幂等网关实现。 */
public final class RedisTestSupport {
    public final Map<String, String> values = new ConcurrentHashMap<>();
    public final StringRedisTemplate redis = mock(StringRedisTemplate.class);
    public final LockTemplate locks;

    @SuppressWarnings("unchecked")
    public RedisTestSupport() throws Exception {
        ValueOperations<String, String> operations = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(operations);
        when(operations.get(anyString())).thenAnswer(call -> values.get(call.getArgument(0)));
        doAnswer(call -> { values.put(call.getArgument(0), call.getArgument(1)); return null; })
                .when(operations).set(anyString(), anyString(), any(Duration.class));
        var mutexes = new ConcurrentHashMap<String, ReentrantLock>();
        var executor = mock(RedissonLockExecutor.class);
        when(executor.renewal()).thenReturn(true);
        when(executor.acquire(anyString(), anyString(), anyLong(), anyLong())).thenAnswer(call -> {
            var mutex = mutexes.computeIfAbsent(call.getArgument(0), key -> new ReentrantLock());
            return mutex.tryLock(3, TimeUnit.SECONDS) ? mock(RLock.class) : null;
        });
        when(executor.releaseLock(anyString(), anyString(), any())).thenAnswer(call -> {
            var mutex = mutexes.get(call.<String>getArgument(0));
            if (mutex == null || !mutex.isHeldByCurrentThread()) { return false; }
            mutex.unlock();
            return true;
        });
        var properties = new Lock4jProperties();
        properties.setPrimaryExecutor(RedissonLockExecutor.class);
        locks = new LockTemplate();
        locks.setExecutors(List.of(executor));
        locks.setProperties(properties);
        locks.afterPropertiesSet();
    }
}

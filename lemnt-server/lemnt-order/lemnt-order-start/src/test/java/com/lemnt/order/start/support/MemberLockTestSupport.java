package com.lemnt.order.start.support;

import com.baomidou.lock.LockTemplate;
import com.baomidou.lock.executor.RedissonLockExecutor;
import com.baomidou.lock.spring.boot.autoconfigure.Lock4jProperties;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.redisson.api.RLock;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Redis锁的测试传输保留事务完成回调，不替代领域锁顺序。
 */
public final class MemberLockTestSupport {
    public static LockTemplate create() throws Exception {
        var locks = new ConcurrentHashMap<String, ReentrantLock>();
        var executor = Mockito.mock(RedissonLockExecutor.class);
        Mockito.when(executor.renewal()).thenReturn(true);
        Mockito.when(
                executor.acquire(
                    ArgumentMatchers.anyString(),
                    ArgumentMatchers.anyString(),
                    ArgumentMatchers.anyLong(),
                    ArgumentMatchers.anyLong()))
            .thenAnswer(
                invocation -> {
                    var lock =
                        locks.computeIfAbsent(
                            invocation.getArgument(0), key -> new ReentrantLock());
                    if (!lock.tryLock(3, TimeUnit.SECONDS)) {
                        return null;
                    }
                    var instance = Mockito.mock(RLock.class);
                    return instance;
                });
        // LockTemplate 调用执行器解锁；测试替身显式释放对应本地锁。
        Mockito.when(
                executor.releaseLock(
                    ArgumentMatchers.anyString(),
                    ArgumentMatchers.anyString(),
                    ArgumentMatchers.any()))
            .thenAnswer(
                invocation -> {
                    var lock = locks.get(invocation.<String>getArgument(0));
                    if (lock == null || !lock.isHeldByCurrentThread()) {
                        return false;
                    }
                    lock.unlock();
                    return true;
                });
        var properties = new Lock4jProperties();
        properties.setPrimaryExecutor(RedissonLockExecutor.class);
        var template = new LockTemplate();
        template.setProperties(properties);
        template.setExecutors(List.of(executor));
        template.afterPropertiesSet();
        return template;
    }
}

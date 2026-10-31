package com.lemnt.member.start;

import com.baomidou.lock.LockInfo;
import com.baomidou.lock.LockTemplate;
import com.baomidou.lock.executor.RedissonLockExecutor;
import com.lemnt.member.infrastructure.growth.gateway.MemberConfigLockGatewayImpl;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

/** 仅替换基础模板的Redis获取/释放，业务仍使用生产事务锁适配。 */
final class MemberConfigLockTestSupport {
    final LockTemplate locks = mock(LockTemplate.class);
    final MemberConfigLockGatewayImpl gateway = new MemberConfigLockGatewayImpl(locks);

    MemberConfigLockTestSupport() {
        var mutex = new ReentrantLock();
        var handles = new ConcurrentHashMap<LockInfo, ReentrantLock>();
        when(locks.lock(anyString(), eq(-1L), eq(3000L), eq(RedissonLockExecutor.class))).thenAnswer(call -> {
            if (!mutex.tryLock(3, TimeUnit.SECONDS)) return null;
            var handle = mock(LockInfo.class);
            handles.put(handle, mutex);
            return handle;
        });
        when(locks.releaseLock(any())).thenAnswer(call -> {
            var held = handles.remove(call.getArgument(0));
            if (held == null || !held.isHeldByCurrentThread()) return false;
            held.unlock();
            return true;
        });
    }
}

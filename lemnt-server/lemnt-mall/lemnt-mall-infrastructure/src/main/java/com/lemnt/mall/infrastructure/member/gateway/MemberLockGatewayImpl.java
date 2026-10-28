package com.lemnt.mall.infrastructure.member.gateway;

import com.lemnt.mall.domain.member.gateway.MemberLockGateway;

import com.baomidou.lock.LockInfo;
import com.baomidou.lock.LockTemplate;
import com.baomidou.lock.executor.RedissonLockExecutor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** 使用基础锁包的 Redisson 锁，覆盖会员操作的完整数据库事务。 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MemberLockGatewayImpl implements MemberLockGateway {
    private final LockTemplate lockTemplate;

    /** 同一事务只获取一次会员锁，事务结束后才释放。 */
    @Override
    public void lockMember(Long memberId) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()
                || !TransactionSynchronizationManager.isSynchronizationActive()) {
            throw new IllegalStateException("会员操作锁必须在事务内获取");
        }
        String key = "lemnt:mall:member:operation:" + memberId;
        boolean held = TransactionSynchronizationManager.getSynchronizations().stream()
                .anyMatch(sync -> sync instanceof MemberLockCompletion completion && key.equals(completion.key()));
        if (held) {
            return;
        }
        // -1 使用 Redisson 看门狗续期，避免远程下单尚未结束时锁固定过期。
        LockInfo lock = lockTemplate.lock(key, -1, 3000, RedissonLockExecutor.class);
        if (lock == null) {
            throw new IllegalStateException("会员操作处理中，请稍后重试");
        }
        TransactionSynchronizationManager.registerSynchronization(new MemberLockCompletion(key, lock, lockTemplate));
    }

    /** 事务提交和回滚均释放锁，不在业务方法返回时提前释放。 */
    private record MemberLockCompletion(String key, LockInfo lock, LockTemplate lockTemplate)
            implements TransactionSynchronization {
        @Override
        public void afterCompletion(int status) {
            if (!lockTemplate.releaseLock(lock)) {
                log.warn("会员操作锁释放失败，key={}", key);
            }
        }
    }
}

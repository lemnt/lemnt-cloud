package com.lemnt.member.infrastructure.growth.gateway;

import com.baomidou.lock.LockInfo;
import com.baomidou.lock.LockTemplate;
import com.baomidou.lock.executor.RedissonLockExecutor;
import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.member.domain.growth.gateway.MemberConfigLockGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/** 基础锁包管理配置锁，锁释放必须晚于数据库事务完成。 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MemberConfigLockGatewayImpl implements MemberConfigLockGateway {
    private static final String KEY = "lemnt:member:growth:configuration";
    private final LockTemplate locks;

    @Override
    public void lockConfiguration() {
        if (!TransactionSynchronizationManager.isActualTransactionActive()
                || !TransactionSynchronizationManager.isSynchronizationActive()) {
            throw new IllegalStateException("配置锁必须在事务内获取");
        }
        if (TransactionSynchronizationManager.getSynchronizations().stream()
                .anyMatch(ConfigurationLockCompletion.class::isInstance)) {
            return;
        }
        var lock = locks.lock(KEY, -1, 3000, RedissonLockExecutor.class);
        if (lock == null) {
            throw new ServiceException("配置正在修改，请稍后重试", 409);
        }
        TransactionSynchronizationManager.registerSynchronization(new ConfigurationLockCompletion(lock, locks));
    }

    /** 提交和回滚均释放基础锁，业务方法返回时不提前解锁。 */
    private record ConfigurationLockCompletion(LockInfo lock, LockTemplate locks) implements TransactionSynchronization {
        @Override
        public void afterCompletion(int status) {
            if (!locks.releaseLock(lock)) {
                log.warn("成长配置锁释放失败，key={}", KEY);
            }
        }
    }
}

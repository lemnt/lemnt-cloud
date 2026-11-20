package com.lemnt.pay.infrastructure.payment.gateway;

import com.lemnt.pay.domain.payment.gateway.WalletOrderLockGateway;

import com.baomidou.lock.LockTemplate;
import com.baomidou.lock.executor.RedissonLockExecutor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Locale;

/** 使用现有 Redisson 看门狗覆盖订单关单与扣款的数据库事务。 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WalletOrderLockGatewayImpl implements WalletOrderLockGateway {
    private final LockTemplate locks;

    @Override
    public void lock(String orderNo) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            throw new IllegalStateException("钱包订单锁需要事务");
        }
        // 订单号在数据库中大小写不敏感，锁也使用同一规范形式。
        String key = "lemnt:order:wallet-payment:" + orderNo.trim().toUpperCase(Locale.ROOT);
        var lock = locks.lock(key, -1, 3000, RedissonLockExecutor.class);
        if (lock == null) throw new IllegalStateException("订单支付或关单处理中，请稍后重试");
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCompletion(int status) {
                        if (!locks.releaseLock(lock)) log.warn("钱包订单锁释放失败，订单 {}", orderNo);
                    }
                });
    }
}

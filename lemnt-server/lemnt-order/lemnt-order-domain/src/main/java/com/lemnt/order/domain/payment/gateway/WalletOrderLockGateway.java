package com.lemnt.order.domain.payment.gateway;

/** 钱包扣款与订单关单共用订单号锁，事务结束后释放。 */
public interface WalletOrderLockGateway {
    void lock(String orderNo);
}

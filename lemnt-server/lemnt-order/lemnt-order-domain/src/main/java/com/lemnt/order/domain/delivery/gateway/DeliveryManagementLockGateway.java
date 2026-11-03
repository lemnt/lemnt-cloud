package com.lemnt.order.domain.delivery.gateway;

/** 配送维护互斥锁，保证编码/名称校验与写入串行，释放晚于事务结束。 */
public interface DeliveryManagementLockGateway {
    void lock();
}

package com.lemnt.mall.domain.checkout.gateway;

/** SettlementAbandon 存储接口。 */
public interface SettlementAbandonGateway {
    /**
     * 已放弃。
     */
    boolean abandoned(Long memberId, String id);

    /**
     * 放弃。
     */
    void abandon(Long memberId, String id);
}

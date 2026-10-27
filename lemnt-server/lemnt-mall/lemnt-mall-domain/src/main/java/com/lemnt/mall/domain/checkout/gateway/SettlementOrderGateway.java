package com.lemnt.mall.domain.checkout.gateway;

/** SettlementOrder 存储接口。 */
public interface SettlementOrderGateway {
    /**
     * 订单编号。
     */
    String orderNo(Long memberId, String id);

    /**
     * 绑定。
     */
    void bind(Long memberId, String id, String orderNo);
}

package com.lemnt.trade.domain.order.gateway;

/** 通过商城服务处理订单事件，不共享商城表或事务。 */
public interface OrderBenefitsGateway {
    /** 库存预占完成。 */
    void reserved(String orderNo);
    /** 已支付。 */
    void paid(String orderNo);
    /** 已关闭。 */
    void closed(String orderNo);
}

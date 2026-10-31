package com.lemnt.trade.domain.order.gateway;

/** 库存与优惠券统一回滚消息发送端口。 */
public interface OrderRollbackGateway {
    /** 已提交关单后发送异步补偿消息。 */
    void publish(String orderNo);
}

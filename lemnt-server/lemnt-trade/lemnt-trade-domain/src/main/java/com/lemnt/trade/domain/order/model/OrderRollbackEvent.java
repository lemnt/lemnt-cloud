package com.lemnt.trade.domain.order.model;

import java.io.Serializable;

/** 库存与优惠券统一回滚消息，订单号同时作为库存操作的幂等标识。 */
public record OrderRollbackEvent(String orderNo) implements Serializable {
    public static final String TOPIC = "trade-order-rollback-topic";
    public static final String GROUP = "trade-order-rollback-consumer-group";
}

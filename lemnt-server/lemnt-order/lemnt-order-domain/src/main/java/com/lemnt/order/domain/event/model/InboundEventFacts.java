package com.lemnt.order.domain.event.model;

import java.math.BigDecimal;

/** 订单消费的资金事实，不暴露支付服务协议。 */
public final class InboundEventFacts {
    private InboundEventFacts() {}

    public record PaymentSucceeded(
        String paymentNo,
        String orderNo,
        Long memberId,
        String channel,
        BigDecimal amount,
        String channelTradeNo) {}

    public record RefundSucceeded(
        String refundNo,
        String afterSaleNo,
        String paymentNo,
        String orderNo,
        Long memberId,
        String channel,
        BigDecimal amount,
        String channelRefundNo) {}
}

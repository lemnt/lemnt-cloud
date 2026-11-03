package com.lemnt.pay.client.event;

/** 现金原路退款事实；不直接改变任何订单或权益。 */
public record PayRefundSuccessEvent(
        String refundNo,
        String afterSaleNo,
        String paymentNo,
        String orderNo,
        Long memberId,
        String channel,
        java.math.BigDecimal amount,
        String channelRefundNo)
        implements java.io.Serializable {}

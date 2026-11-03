package com.lemnt.pay.client.event;

/** 渠道已核验的现金支付事实，订单状态及权益由order决定。 */
public record PaySuccessEvent(
        String paymentNo,
        String orderNo,
        Long memberId,
        String channel,
        java.math.BigDecimal amount,
        String channelTradeNo)
        implements java.io.Serializable {}

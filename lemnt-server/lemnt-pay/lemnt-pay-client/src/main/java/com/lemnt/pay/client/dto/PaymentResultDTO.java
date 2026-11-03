package com.lemnt.pay.client.dto;

/** 可信内部支付流水快照，金额单位元。 */
public record PaymentResultDTO(
        String paymentNo,
        String orderNo,
        String channel,
        String appKey,
        String channelTradeNo,
        java.math.BigDecimal amount,
        String status)
        implements java.io.Serializable {}

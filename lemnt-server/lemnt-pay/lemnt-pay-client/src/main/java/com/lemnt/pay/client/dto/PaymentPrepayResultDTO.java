package com.lemnt.pay.client.dto;

/** 渠道预支付参数，金额单位元。 */
public record PaymentPrepayResultDTO(
        String paymentNo,
        String orderNo,
        String channel,
        java.math.BigDecimal amount,
        String payUrl,
        java.util.Map<String, String> params)
        implements java.io.Serializable {}

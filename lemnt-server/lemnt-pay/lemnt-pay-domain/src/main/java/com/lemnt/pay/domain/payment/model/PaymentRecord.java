package com.lemnt.pay.domain.payment.model;

import java.math.BigDecimal;

/**
 * 支付记录。
 */
public record PaymentRecord(
        Long id,
        String paymentNo,
        String orderNo,
        String channel,
        String appKey,
        String channelTradeNo,
        BigDecimal amount,
        String status,
        String notifyId,
        BigDecimal channelFeeRate,
        BigDecimal channelFeeAmount,
        Long expiresAtEpochSecond,
        String notifyUrl) {
    /** 兼容没有手续费及有效期快照的支付记录。 */
    public PaymentRecord(
            Long id,
            String paymentNo,
            String orderNo,
            String channel,
            String appKey,
            String channelTradeNo,
            BigDecimal amount,
            String status,
            String notifyId) {
        this(
                id,
                paymentNo,
                orderNo,
                channel,
                appKey,
                channelTradeNo,
                amount,
                status,
                notifyId,
                null,
                null,
                null,
                null);
    }
}

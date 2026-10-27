package com.lemnt.trade.domain.payment.model;

import java.math.BigDecimal;

/**
 * 支付记录。
 */
public record PaymentRecord(
        Long id,
        String paymentNo,
        String orderNo,
        String channel,
        String channelTradeNo,
        BigDecimal amount,
        String status,
        String notifyId
) {}

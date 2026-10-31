package com.lemnt.trade.domain.payment.model;

import java.math.BigDecimal;

/**
 * Construct this value only after the payment-channel adapter verifies signature,
 * merchant identity, currency, timestamp and replay protection.
 */
public record VerifiedPaymentNotification(
        String paymentNo,
        String orderNo,
        String channel,
        String channelTradeNo,
        BigDecimal amount,
        String notifyId
) {}

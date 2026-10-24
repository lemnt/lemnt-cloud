package com.lemnt.trade.domain.model;

import java.math.BigDecimal;

public record PaymentRecord(Long id, String paymentNo, String orderNo, String channel,
                            String channelTradeNo, BigDecimal amount, String status, String notifyId) { }
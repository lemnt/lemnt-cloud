package com.lemnt.trade.application.payment.dto;

import java.io.Serializable;
import java.math.BigDecimal;

/** 支付单管理查询结果。 */
public record PaymentDTO(
        Long id,
        String paymentNo,
        String orderNo,
        String channel,
        String channelTradeNo,
        BigDecimal amount,
        String status
) implements Serializable {
}

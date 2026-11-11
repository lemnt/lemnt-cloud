package com.lemnt.pay.client.dto;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 可信内部支付流水快照，金额单位元。
 */
public record PaymentResultDTO(
    String paymentNo,
    String orderNo,
    String channel,
    String appKey,
    String channelTradeNo,
    BigDecimal amount,
    String status)
    implements Serializable {
}

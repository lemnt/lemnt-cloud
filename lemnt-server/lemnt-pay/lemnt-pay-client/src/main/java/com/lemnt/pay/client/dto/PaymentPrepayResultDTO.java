package com.lemnt.pay.client.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;

/**
 * 渠道预支付参数，金额单位元。
 */
public record PaymentPrepayResultDTO(
    String paymentNo,
    String orderNo,
    String channel,
    BigDecimal amount,
    String payUrl,
    Map<String, String> params)
    implements Serializable {
}

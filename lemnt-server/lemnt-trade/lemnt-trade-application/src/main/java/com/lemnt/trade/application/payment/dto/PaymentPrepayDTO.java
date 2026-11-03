package com.lemnt.trade.application.payment.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;

/** 预支付输出，客户端凭 payUrl 或 params 发起渠道支付。 */
public record PaymentPrepayDTO(
        String paymentNo,
        String orderNo,
        String channel,
        BigDecimal amount,
        String payUrl,
        Map<String, String> params)
        implements Serializable {
    public PaymentPrepayDTO {
        params = params == null ? Map.of() : Map.copyOf(params);
    }
}

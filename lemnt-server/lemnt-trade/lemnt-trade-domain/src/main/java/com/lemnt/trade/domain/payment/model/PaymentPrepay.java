package com.lemnt.trade.domain.payment.model;

import java.util.Map;

/** 渠道预支付结果：客户端据此发起支付，渠道密钥不得出现在参数中。 */
public record PaymentPrepay(
        String paymentNo,
        String channel,
        String payUrl,
        Map<String, String> params
) {
    public PaymentPrepay {
        params = params == null ? Map.of() : Map.copyOf(params);
    }
}

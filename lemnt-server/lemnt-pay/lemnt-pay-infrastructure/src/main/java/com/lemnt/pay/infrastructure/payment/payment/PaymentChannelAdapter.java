package com.lemnt.pay.infrastructure.payment.payment;

import com.lemnt.pay.domain.payment.model.PayChannelConfig;
import com.lemnt.pay.domain.payment.model.PaymentPrepay;
import com.lemnt.pay.domain.payment.model.PaymentRecord;
import com.lemnt.pay.domain.payment.model.VerifiedPaymentNotification;

/** 具体渠道适配器负责签名、密钥解析、超时和渠道幂等。 */
public interface PaymentChannelAdapter {
    /** 返回渠道编码。 */
    String channel();

    /** 生成预支付参数。 */
    PaymentPrepay prepay(PaymentRecord payment, PayChannelConfig config);

    /** 验签并转换通知；签名、时间戳或商户不一致时必须抛错。 */
    VerifiedPaymentNotification verify(
            PayChannelConfig config, String body, String signature, String timestamp);
}

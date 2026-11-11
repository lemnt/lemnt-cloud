package com.lemnt.pay.domain.channel.gateway;

import com.lemnt.pay.domain.channel.model.PayChannelConfig;
import com.lemnt.pay.domain.channel.model.PaymentChannelNotification;
import com.lemnt.pay.domain.payment.model.PaymentPrepay;
import com.lemnt.pay.domain.payment.model.PaymentRecord;
import com.lemnt.pay.domain.payment.model.VerifiedPaymentNotification;

/**
 * 支付渠道网关：预支付参数生成与回调验签，未接入渠道必须明确失败。
 */
public interface PaymentChannelGateway {
    /**
     * 生成渠道预支付参数；payerOpenId 为渠道要求的付款人标识，可为空。
     */
    PaymentPrepay prepay(PaymentRecord payment, PayChannelConfig config, String payerOpenId);

    /**
     * 验签并转换渠道通知；验签失败不得返回已核验对象。
     */
    VerifiedPaymentNotification verify(
        String channel,
        PayChannelConfig config,
        PaymentChannelNotification notification);
}

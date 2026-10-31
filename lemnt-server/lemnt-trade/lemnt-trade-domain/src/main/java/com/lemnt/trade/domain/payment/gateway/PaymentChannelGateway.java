package com.lemnt.trade.domain.payment.gateway;

import com.lemnt.trade.domain.payment.model.PayChannelConfig;
import com.lemnt.trade.domain.payment.model.PaymentPrepay;
import com.lemnt.trade.domain.payment.model.PaymentRecord;
import com.lemnt.trade.domain.payment.model.VerifiedPaymentNotification;

/** 支付渠道网关：预支付参数生成与回调验签，未接入渠道必须明确失败。 */
public interface PaymentChannelGateway {
    /** 生成渠道预支付参数。 */
    PaymentPrepay prepay(PaymentRecord payment, PayChannelConfig config);

    /** 验签并转换渠道通知；验签失败不得返回已核验对象。 */
    VerifiedPaymentNotification verify(String channel, PayChannelConfig config, String body,
            String signature, String timestamp);
}

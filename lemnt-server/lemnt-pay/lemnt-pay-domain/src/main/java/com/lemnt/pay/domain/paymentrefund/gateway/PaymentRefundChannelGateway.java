package com.lemnt.pay.domain.paymentrefund.gateway;

import com.lemnt.pay.domain.channel.model.PaymentChannelNotification;
import com.lemnt.pay.domain.payment.model.PaymentRecord;
import com.lemnt.pay.domain.paymentrefund.model.PaymentRefund;
import com.lemnt.pay.domain.paymentrefund.model.PaymentRefundResult;

/**
 * 原路退款渠道调用接口。
 */
public interface PaymentRefundChannelGateway {
    /**
     * 始终使用原退款单号作为渠道幂等键，响应未知时不得生成新单号。
     */
    PaymentRefundResult refund(PaymentRefund refund, PaymentRecord payment);

    /** 只查原退款单，渠道无记录时返回空。 */
    PaymentRefundResult query(PaymentRefund refund, PaymentRecord payment);

    /** 以回调路径的原应用、渠道配置验签。 */
    PaymentRefundResult verify(String appKey, String channel, PaymentChannelNotification notification);
}

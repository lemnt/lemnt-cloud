package com.lemnt.trade.domain.paymentrefund.gateway;

import com.lemnt.trade.domain.payment.model.PaymentRecord;
import com.lemnt.trade.domain.paymentrefund.model.PaymentRefund;
import com.lemnt.trade.domain.paymentrefund.model.PaymentRefundResult;

/** 原路退款渠道调用接口。 */
public interface PaymentRefundChannelGateway {
    /** 始终使用原退款单号作为渠道幂等键，响应未知时不得生成新单号。 */
    PaymentRefundResult refund(PaymentRefund refund, PaymentRecord payment);
}

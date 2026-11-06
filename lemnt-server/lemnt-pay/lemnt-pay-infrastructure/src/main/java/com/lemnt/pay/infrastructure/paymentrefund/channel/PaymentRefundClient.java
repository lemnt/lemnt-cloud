package com.lemnt.pay.infrastructure.paymentrefund.channel;

import com.lemnt.pay.domain.payment.model.PayChannelConfig;
import com.lemnt.pay.domain.payment.model.PaymentRecord;
import com.lemnt.pay.domain.paymentrefund.model.PaymentRefund;
import com.lemnt.pay.domain.paymentrefund.model.PaymentRefundResult;

/**
 * 具体渠道适配器负责验签、商户核验、网络超时和渠道幂等。
 */
public interface PaymentRefundClient {
    /**
     * 返回渠道编码。
     */
    String channel();

    /**
     * 查询原退款单，未查到返回空；超时抛错，不把未知结果视作失败。
     */
    PaymentRefundResult query(
        PaymentRefund refund, PaymentRecord payment, PayChannelConfig config);

    /**
     * 原退款编号重试必须查询或重放同一渠道交易，不能创建第二笔退款。
     */
    PaymentRefundResult refund(
        PaymentRefund refund, PaymentRecord payment, PayChannelConfig config);
}

package com.lemnt.pay.client;

import com.lemnt.pay.client.bo.PaymentPrepayBo;
import com.lemnt.pay.client.bo.PaymentRefundBo;
import com.lemnt.pay.client.dto.PaymentPrepayResultDTO;
import com.lemnt.pay.client.dto.PaymentRefundResultDTO;

/** 可信内部支付资金操作；不拥有订单及售后状态。 */
public interface PaymentClient {
    PaymentPrepayResultDTO prepay(PaymentPrepayBo input);

    PaymentRefundResultDTO refund(PaymentRefundBo input);
}

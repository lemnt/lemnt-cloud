package com.lemnt.order.domain.payment.gateway;

import com.lemnt.order.domain.aftersale.model.AfterSale;
import com.lemnt.order.domain.paymentrefund.model.PaymentRefund;

/** 只有订单已登记并冻结的非零售后请求才交给资金服务。 */
public interface PaymentGateway {
    PaymentRefund refund(AfterSale frozen);
}

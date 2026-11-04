package com.lemnt.order.domain.paymentrefund.gateway;

import com.lemnt.order.domain.paymentrefund.model.PaymentRefund;
import com.lemnt.order.domain.paymentrefund.model.PaymentRefundTotal;

/** 只读pay的真实资金退款结果，不拥有退款流水存储。 */
public interface PaymentRefundGateway {
    PaymentRefundTotal total(String orderNo);

    PaymentRefund findByAfterSaleNo(String no);
}

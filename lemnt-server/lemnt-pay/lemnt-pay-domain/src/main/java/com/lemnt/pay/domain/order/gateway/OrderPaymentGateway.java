package com.lemnt.pay.domain.order.gateway;

import com.lemnt.pay.domain.order.model.FrozenRefundPlan;
import com.lemnt.pay.domain.order.model.OrderPaymentSnapshot;
import com.lemnt.pay.domain.payment.model.PaymentOperator;

/** 支付侧只通过可信订单契约核对归属，不共享事务或Mapper。 */
public interface OrderPaymentGateway {
    OrderPaymentSnapshot findByOrderNo(String orderNo);

    FrozenRefundPlan prepareRefund(String afterSaleNo, PaymentOperator operator);

    FrozenRefundPlan refundPlan(String afterSaleNo);
}

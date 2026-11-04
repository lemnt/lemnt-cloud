package com.lemnt.order.domain.payment.gateway;

import com.lemnt.order.domain.payment.model.PaymentRecord;

import java.util.List;

/** 只读取pay资金事实，不能跨库更新或创建支付。 */
public interface TradePaymentGateway {
    List<PaymentRecord> findByOrderNo(String orderNo);

    PaymentRecord findByPaymentNo(String paymentNo);
}

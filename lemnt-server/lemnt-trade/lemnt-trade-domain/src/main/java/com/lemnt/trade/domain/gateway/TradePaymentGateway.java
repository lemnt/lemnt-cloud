package com.lemnt.trade.domain.gateway;

import com.lemnt.trade.domain.model.PaymentRecord;
import com.lemnt.trade.domain.model.VerifiedPaymentNotification;

public interface TradePaymentGateway {
    PaymentRecord findByPaymentNo(String paymentNo);
    PaymentRecord createWaiting(String paymentNo, String orderNo, String channel, java.math.BigDecimal amount);
    boolean markSuccess(VerifiedPaymentNotification notification);
}
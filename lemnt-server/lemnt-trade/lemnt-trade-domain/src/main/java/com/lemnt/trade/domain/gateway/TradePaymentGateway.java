package com.lemnt.trade.domain.gateway;

import com.lemnt.trade.domain.model.PaymentRecord;
import com.lemnt.trade.domain.model.VerifiedPaymentNotification;
import java.math.BigDecimal;

public interface TradePaymentGateway {
    PaymentRecord findByPaymentNo(String paymentNo);
    PaymentRecord findByOrderNoAndChannel(String orderNo, String channel);
    PaymentRecord createWaiting(String paymentNo, String orderNo, String channel, BigDecimal amount);
    boolean markSuccess(VerifiedPaymentNotification notification);
}
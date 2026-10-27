package com.lemnt.trade.domain.payment.gateway;

import com.lemnt.trade.domain.payment.model.PaymentRecord;
import com.lemnt.trade.domain.payment.model.VerifiedPaymentNotification;

import java.math.BigDecimal;
import java.util.List;

/**
 * 交易支付领域网关。
 */
public interface TradePaymentGateway {
    /**
     * 查找按支付编号。
     */
    PaymentRecord findByPaymentNo(String paymentNo);

    /**
     * 查找按订单编号及支付渠道。
     */
    PaymentRecord findByOrderNoAndChannel(String orderNo, String channel);

    /**
     * 创建待支付记录。
     */
    PaymentRecord createWaiting(String paymentNo, String orderNo, String channel, BigDecimal amount);

    /**
     * 查找按订单编号。
     */
    List<PaymentRecord> findByOrderNo(String orderNo);

    /**
     * 标记关闭。
     */
    boolean markClosed(String paymentNo);

    /**
     * 标记成功。
     */
    boolean markSuccess(VerifiedPaymentNotification notification);
}

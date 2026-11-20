package com.lemnt.pay.domain.paymentrefund.model;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 支付退款交易。
 */
public record PaymentRefund(
        String refundNo,
        String afterSaleNo,
        String paymentNo,
        String orderNo,
        String channel,
        BigDecimal amount,
        String status,
        String channelRefundNo,
        String notifyUrl)
        implements Serializable {
    /** 兼容没有业务回调地址的退款记录。 */
    public PaymentRefund(
            String refundNo,
            String afterSaleNo,
            String paymentNo,
            String orderNo,
            String channel,
            BigDecimal amount,
            String status,
            String channelRefundNo) {
        this(
                refundNo,
                afterSaleNo,
                paymentNo,
                orderNo,
                channel,
                amount,
                status,
                channelRefundNo,
                null);
    }
}

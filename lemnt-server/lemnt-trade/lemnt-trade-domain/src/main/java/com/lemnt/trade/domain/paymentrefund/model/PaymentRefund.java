package com.lemnt.trade.domain.paymentrefund.model;

import java.io.Serializable;
import java.math.BigDecimal;

/** 支付退款交易。 */
public record PaymentRefund(
        String refundNo,
        String afterSaleNo,
        String paymentNo,
        String orderNo,
        String channel,
        BigDecimal amount,
        String status,
        String channelRefundNo
) implements Serializable {
}

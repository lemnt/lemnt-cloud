package com.lemnt.trade.application.paymentrefund.dto;

import java.io.Serializable;
import java.math.BigDecimal;

/** 支付退款管理结果。 */
public record PaymentRefundDTO(
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

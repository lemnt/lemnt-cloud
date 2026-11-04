package com.lemnt.pay.client.dto;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 原路退款资金事实；零现金结果refundNo为空、channel为NONE。
 */
public record PaymentRefundResultDTO(
    String refundNo,
    String afterSaleNo,
    String paymentNo,
    String orderNo,
    String channel,
    BigDecimal amount,
    String status,
    String channelRefundNo)
    implements Serializable {
}

package com.lemnt.trade.domain.paymentrefund.model;

import java.io.Serializable;
import java.math.BigDecimal;

/** 成功退款交易汇总。 */
public record PaymentRefundTotal(
        long refundCount,
        BigDecimal refundAmount
) implements Serializable {
}

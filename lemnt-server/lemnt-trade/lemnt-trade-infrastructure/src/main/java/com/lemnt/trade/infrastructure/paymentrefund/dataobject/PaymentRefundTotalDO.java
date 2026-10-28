package com.lemnt.trade.infrastructure.paymentrefund.dataobject;

import java.io.Serializable;
import java.math.BigDecimal;

/** 支付退款汇总查询投影。 */
public record PaymentRefundTotalDO(
        long refundCount,
        BigDecimal refundAmount
) implements Serializable {
}

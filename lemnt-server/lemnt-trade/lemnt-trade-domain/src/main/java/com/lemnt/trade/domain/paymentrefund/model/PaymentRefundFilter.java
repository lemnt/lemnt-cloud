package com.lemnt.trade.domain.paymentrefund.model;

import java.io.Serializable;

/** 支付退款查询条件。 */
public record PaymentRefundFilter(
        String afterSaleNo,
        String paymentNo,
        String status,
        int pageNum,
        int pageSize
) implements Serializable {
}

package com.lemnt.trade.domain.payment.model;

import java.io.Serializable;

/** 支付单查询条件。 */
public record PaymentFilter(
        String paymentNo,
        String orderNo,
        String channel,
        String status,
        int pageNum,
        int pageSize
) implements Serializable {
}

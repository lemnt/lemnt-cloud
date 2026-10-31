package com.lemnt.trade.application.payment.qry;

import java.io.Serializable;

/** 支付单分页查询。 */
public record PaymentPageQry(
        String paymentNo,
        String orderNo,
        String channel,
        String status,
        int pageNum,
        int pageSize
) implements Serializable {
}

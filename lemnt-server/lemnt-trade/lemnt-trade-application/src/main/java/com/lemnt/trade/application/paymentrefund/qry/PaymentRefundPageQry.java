package com.lemnt.trade.application.paymentrefund.qry;

import java.io.Serializable;

/** 支付退款分页查询。 */
public record PaymentRefundPageQry(
        String afterSaleNo,
        String paymentNo,
        String status,
        int pageNum,
        int pageSize
) implements Serializable {
}

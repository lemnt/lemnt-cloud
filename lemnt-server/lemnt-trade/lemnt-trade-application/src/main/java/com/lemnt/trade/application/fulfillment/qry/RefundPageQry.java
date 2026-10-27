package com.lemnt.trade.application.fulfillment.qry;

/**
 * 退款分页查询。
 */
public record RefundPageQry(
        String orderNo,
        String status,
        int pageNum,
        int pageSize
) implements java.io.Serializable {}

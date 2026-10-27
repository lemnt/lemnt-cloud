package com.lemnt.trade.application.fulfillment.qry;

/**
 * 订单分页查询。
 */
public record OrderPageQry(
        String orderNo,
        String status,
        int pageNum,
        int pageSize
) implements java.io.Serializable {}

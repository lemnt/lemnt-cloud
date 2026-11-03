package com.lemnt.order.application.fulfillment.qry;

import java.io.Serializable;

/**
 * 退款分页查询。
 */
public record RefundPageQry(String orderNo, String status, int pageNum, int pageSize)
        implements Serializable {}

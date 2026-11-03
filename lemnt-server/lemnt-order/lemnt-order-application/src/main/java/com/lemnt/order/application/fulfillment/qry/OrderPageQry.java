package com.lemnt.order.application.fulfillment.qry;

import java.io.Serializable;

/**
 * 订单分页查询。
 */
public record OrderPageQry(String orderNo, String status, int pageNum, int pageSize)
        implements Serializable {}

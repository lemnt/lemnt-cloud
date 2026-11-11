package com.lemnt.common.client.event;

import java.util.Set;

/**
 * 资金事实由pay发布，订单及售后事实由order校验后发布。
 */
public final class CommerceEventTopics {
    public static final String PAY_SUCCESS = "PAY_SUCCESS";
    public static final String PAY_REFUND_SUCCESS = "PAY_REFUND_SUCCESS";
    public static final String ORDER_PAID = "ORDER_PAID";
    public static final String AFTER_SALE_COMPLETED = "AFTER_SALE_COMPLETED";
    public static final String ORDER_CLOSED = "ORDER_CLOSED";
    public static final String ORDER_SHIPPED = "ORDER_SHIPPED";
    public static final String ORDER_COMPLETED = "ORDER_COMPLETED";
    public static final String STOCK_PRE_RESERVED = "STOCK_PRE_RESERVED";
    public static final Set<String> ALL =
        Set.of(
            PAY_SUCCESS,
            PAY_REFUND_SUCCESS,
            ORDER_PAID,
            AFTER_SALE_COMPLETED,
            ORDER_CLOSED,
            ORDER_SHIPPED,
            ORDER_COMPLETED,
            STOCK_PRE_RESERVED);

    private CommerceEventTopics() {
    }
}

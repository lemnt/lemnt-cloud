package com.lemnt.trade.domain.fulfillment.model;

/**
 * 履约筛选。
 */
public record FulfillmentFilter(
        String orderNo,
        String status,
        int offset,
        int limit
) {}

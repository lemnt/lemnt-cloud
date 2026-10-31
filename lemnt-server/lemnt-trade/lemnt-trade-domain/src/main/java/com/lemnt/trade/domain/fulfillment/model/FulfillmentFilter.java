package com.lemnt.trade.domain.fulfillment.model;

/**
 * 履约筛选。memberId 用于会员本人订单，后台传空；commentStatus 区分待评价与已完成。
 */
public record FulfillmentFilter(
        String orderNo,
        String status,
        Boolean commentStatus,
        Long memberId,
        int offset,
        int limit
) {}

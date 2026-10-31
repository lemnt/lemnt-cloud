package com.lemnt.trade.domain.order.model;

/**
 * 订单状态数量：按状态与评价状态分组，避免待评价和已完成被合并。
 */
public record OrderStatusCount(
        String status,
        Boolean commentStatus,
        long count
) {}

package com.lemnt.trade.domain.order.model;

/**
 * 订单状态数量：按状态与评价状态分组，避免待评价和已完成被合并。
 */
public record OrderStatusCount(
        String status,
        Boolean commentStatus,
        Integer deliveryType,
        long count
) {
    /** 兼容未区分配送方式的历史统计。 */
    public OrderStatusCount(String status, Boolean commentStatus, long count) {
        this(status, commentStatus, 1, count);
    }
}

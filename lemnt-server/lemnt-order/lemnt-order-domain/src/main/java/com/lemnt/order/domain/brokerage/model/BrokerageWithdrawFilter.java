package com.lemnt.order.domain.brokerage.model;

/**
 * 提现分页条件。
 */
public record BrokerageWithdrawFilter(Long userId, Integer status, int pageNum, int pageSize) {
}

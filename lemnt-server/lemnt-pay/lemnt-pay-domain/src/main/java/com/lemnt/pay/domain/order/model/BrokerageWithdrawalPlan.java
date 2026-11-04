package com.lemnt.pay.domain.order.model;

import java.math.BigDecimal;

/**
 * 订单服务拥有的审批事实。
 */
public record BrokerageWithdrawalPlan(
    Long id, Long memberId, BigDecimal amount, Integer type, Integer status) {
}

package com.lemnt.pay.domain.order.gateway;

import com.lemnt.pay.domain.order.model.BrokerageWithdrawalPlan;

/**
 * 只读原审批计划，支付不能修改佣金或审批状态。
 */
public interface BrokerageWithdrawalGateway {
    BrokerageWithdrawalPlan find(Long id);
}

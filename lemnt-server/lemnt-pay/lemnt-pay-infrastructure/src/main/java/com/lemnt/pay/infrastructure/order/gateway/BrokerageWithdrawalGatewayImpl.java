package com.lemnt.pay.infrastructure.order.gateway;

import com.lemnt.order.client.OrderSnapshotClient;
import com.lemnt.pay.domain.order.gateway.BrokerageWithdrawalGateway;
import com.lemnt.pay.domain.order.model.BrokerageWithdrawalPlan;

import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

/**
 * 原审核单RPC核验，缺失时拒绝入账。
 */
@Component
public class BrokerageWithdrawalGatewayImpl implements BrokerageWithdrawalGateway {
    @DubboReference(check = false, timeout = 5000, retries = 0)
    private OrderSnapshotClient client;

    public BrokerageWithdrawalPlan find(Long id) {
        var v = client.brokerageWithdrawal(id);
        return v == null
            ? null
            : new BrokerageWithdrawalPlan(
            v.id(), v.memberId(), v.amount(), v.type(), v.status());
    }
}

package com.lemnt.member.infrastructure.growth.gateway;

import com.lemnt.member.domain.growth.gateway.OrderBenefitsSnapshotGateway;
import com.lemnt.member.domain.growth.model.OrderBenefitsSnapshot;
import com.lemnt.order.client.OrderSnapshotClient;

import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

/** 订单只读快照，不共享订单库或事务。 */
@Component
public class OrderBenefitsSnapshotGatewayImpl implements OrderBenefitsSnapshotGateway {
    @DubboReference(check = false, timeout = 5000, retries = 0)
    private OrderSnapshotClient client;

    public OrderBenefitsSnapshot find(String no) {
        var v = client.benefitsSnapshot(no);
        return v == null
                ? null
                : new OrderBenefitsSnapshot(
                        v.orderNo(),
                        v.memberId(),
                        v.usePoint(),
                        v.givePoint(),
                        v.amount(),
                        v.status());
    }
}

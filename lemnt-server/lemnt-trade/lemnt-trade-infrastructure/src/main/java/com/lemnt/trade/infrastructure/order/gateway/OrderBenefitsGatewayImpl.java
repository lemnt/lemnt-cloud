package com.lemnt.trade.infrastructure.order.gateway;

import com.lemnt.mall.client.CheckoutLifecycleClient;
import com.lemnt.trade.domain.order.gateway.OrderBenefitsGateway;

import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

/** 由事务消息驱动，商城故障时保留事件重试。 */
@Component
public class OrderBenefitsGatewayImpl implements OrderBenefitsGateway {
    @DubboReference(check = false, timeout = 5000, retries = 0)
    private CheckoutLifecycleClient client;

    @Override
    public void reserved(String orderNo) {
        client.reserved(orderNo);
    }
    @Override
    public void paid(String orderNo) {
        client.paid(orderNo);
    }
    @Override
    public void closed(String orderNo) {
        client.closed(orderNo);
    }
}

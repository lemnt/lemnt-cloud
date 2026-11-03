package com.lemnt.order.infrastructure.order.gateway;

import com.lemnt.order.client.CheckoutLifecycleClient;
import com.lemnt.order.client.bo.RefundBenefitsBo;
import com.lemnt.order.client.bo.RefundStockBo;
import com.lemnt.order.domain.order.gateway.OrderBenefitsGateway;
import com.lemnt.order.domain.order.model.OrderRefundBenefits;

import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 由事务消息驱动，商城故障时保留事件重试。
 */
@Component
public class OrderBenefitsGatewayImpl implements OrderBenefitsGateway {
    @DubboReference(check = false, timeout = 5000, retries = 0)
    private CheckoutLifecycleClient client;

    @Override
    public void verify(String orderNo) {
        client.verify(orderNo);
    }

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

    @Override
    public void refunded(
            String orderNo,
            String refundNo,
            Map<Long, Integer> quantities,
            BigDecimal refundAmount) {
        client.refunded(
                orderNo,
                refundNo,
                quantities.entrySet().stream()
                        .map(item -> new RefundStockBo(item.getKey(), item.getValue()))
                        .toList(),
                refundAmount);
    }

    @Override
    public void refunded(OrderRefundBenefits plan) {
        client.refunded(
                new RefundBenefitsBo(
                        plan.orderNo(),
                        plan.afterSaleNo(),
                        plan.orderItemId(),
                        plan.skuId(),
                        plan.quantity(),
                        plan.amount(),
                        plan.returnUsePoint(),
                        plan.deductGivePoint(),
                        plan.ruleVersion()));
    }
}

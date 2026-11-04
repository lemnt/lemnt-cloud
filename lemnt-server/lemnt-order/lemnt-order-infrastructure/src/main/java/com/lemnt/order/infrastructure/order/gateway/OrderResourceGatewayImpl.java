package com.lemnt.order.infrastructure.order.gateway;

import com.lemnt.order.domain.order.gateway.OrderResourceGateway;
import com.lemnt.order.domain.order.gateway.TradeOrderGateway;
import com.lemnt.order.domain.order.gateway.TradeOrderItemGateway;
import com.lemnt.order.domain.order.model.OrderResourcePlan;
import com.lemnt.order.domain.promotion.gateway.OrderPromotionGateway;
import com.lemnt.order.domain.promotion.gateway.PromotionOrderGateway;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 只查询订单库的原始关联，活动结果ID仍保存于扩展表，不混入不可变资源指纹。
 */
@Component
@RequiredArgsConstructor
public class OrderResourceGatewayImpl implements OrderResourceGateway {
    private final TradeOrderGateway orders;
    private final TradeOrderItemGateway items;
    private final OrderPromotionGateway promotions;
    private final PromotionOrderGateway activities;

    public OrderResourcePlan plan(String no) {
        var order = orders.findHeader(no);
        if (order == null) throw new IllegalStateException("原订单不存在");
        var activity = activities.findByOrderNo(no);
        Long coupon =
            promotions.find(order.id()).stream()
                .filter(p -> "COUPON".equals(p.promotionType()))
                .map(p -> p.sourceId())
                .findFirst()
                .orElse(null);
        return new OrderResourcePlan(
            order.id(),
            no,
            order.memberId(),
            coupon,
            activity == null ? null : activity.activityType(),
            activity == null ? null : activity.activityId(),
            activity != null && "BARGAIN".equals(activity.activityType())
                ? activity.activityRecordId()
                : null,
            activity == null ? null : activity.headId(),
            items.items(order.id()).stream()
                .map(
                    v ->
                        new OrderResourcePlan.Line(
                            v.spuId(), v.productId(), v.quantity()))
                .toList(),
            order.status());
    }
}

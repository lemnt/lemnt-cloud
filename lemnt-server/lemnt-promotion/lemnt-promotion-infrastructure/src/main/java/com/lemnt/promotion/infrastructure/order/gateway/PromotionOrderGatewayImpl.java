package com.lemnt.promotion.infrastructure.order.gateway;

import com.lemnt.order.client.OrderSnapshotClient;
import com.lemnt.promotion.domain.order.gateway.PromotionOrderGateway;
import com.lemnt.promotion.domain.order.model.PromotionOrderResources;

import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

/** 当前状态可变化，资源标识只来自原订单，不查询商品/券当前配置补历史。 */
@Component
public class PromotionOrderGatewayImpl implements PromotionOrderGateway {
    @DubboReference(check = false, timeout = 5000, retries = 0)
    private OrderSnapshotClient client;

    @Override
    public PromotionOrderResources resources(String no) {
        var v = client.promotionResources(no);
        if (v == null || v.lines() == null) throw new IllegalStateException("原订单冻结资源缺失");
        return new PromotionOrderResources(
                v.orderId(),
                v.orderNo(),
                v.memberId(),
                v.couponId(),
                v.activityType(),
                v.activityId(),
                v.activityRecordId(),
                v.headId(),
                v.lines().stream()
                        .map(
                                i ->
                                        new PromotionOrderResources.Line(
                                                i.spuId(), i.skuId(), i.quantity()))
                        .toList(),
                v.status());
    }
}

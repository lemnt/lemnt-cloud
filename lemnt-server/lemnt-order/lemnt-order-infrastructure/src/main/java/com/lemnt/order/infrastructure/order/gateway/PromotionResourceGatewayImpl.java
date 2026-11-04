package com.lemnt.order.infrastructure.order.gateway;

import com.lemnt.order.domain.order.gateway.PromotionResourceGateway;
import com.lemnt.order.domain.order.model.OrderResourcePlan;
import com.lemnt.promotion.client.PromotionLockClient;
import com.lemnt.promotion.client.bo.PromotionLockBo;

import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

/** 独立营销库持有资源锁，订单不读取营销表。 */
@Component
public class PromotionResourceGatewayImpl implements PromotionResourceGateway {
    @DubboReference(check = false, timeout = 5000, retries = 0)
    private PromotionLockClient client;

    public void lock(OrderResourcePlan v) {
        client.lock(
                new PromotionLockBo(
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
                                        l ->
                                                new PromotionLockBo.Line(
                                                        l.spuId(), l.skuId(), l.quantity()))
                                .toList()));
    }

    public Long paidRecord(String no) {
        var v = client.query(no);
        return v == null ? null : v.activityRecordId();
    }
}

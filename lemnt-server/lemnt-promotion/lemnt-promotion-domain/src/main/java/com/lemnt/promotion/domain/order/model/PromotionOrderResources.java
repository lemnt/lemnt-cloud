package com.lemnt.promotion.domain.order.model;

import java.io.Serializable;
import java.util.List;

/** 营销防腐层的原订单资源计划，只读取可信快照。 */
public record PromotionOrderResources(
        Long orderId,
        String orderNo,
        Long memberId,
        Long couponId,
        String activityType,
        Long activityId,
        Long activityRecordId,
        Long headId,
        List<Line> lines,
        String status) {
    public record Line(Long spuId, Long skuId, int quantity) implements Serializable {}
}

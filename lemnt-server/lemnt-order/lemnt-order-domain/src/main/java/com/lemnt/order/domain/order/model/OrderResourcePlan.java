package com.lemnt.order.domain.order.model;

import java.util.List;

/**
 * 原订单资源标识，拼团生成的参与记录不属于创建时资源指纹。
 */
public record OrderResourcePlan(
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
    public record Line(Long spuId, Long skuId, int quantity) {
    }
}

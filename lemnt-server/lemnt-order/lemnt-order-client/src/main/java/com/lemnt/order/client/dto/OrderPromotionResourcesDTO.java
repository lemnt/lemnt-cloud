package com.lemnt.order.client.dto;

import java.io.Serializable;
import java.util.List;

/** 原订单的资源标识，不包含当前营销规则；status只作校验，不参与冻结资源哈希。 */
public record OrderPromotionResourcesDTO(
        Long orderId,
        String orderNo,
        Long memberId,
        Long couponId,
        String activityType,
        Long activityId,
        Long activityRecordId,
        Long headId,
        List<Line> lines,
        String status)
        implements Serializable {
    public record Line(Long spuId, Long skuId, int quantity) implements Serializable {}
}

package com.lemnt.order.domain.promotion.model;

import java.util.List;

/**
 * 订单向营销请求新活动报价，价格及积分结果不能从HTTP输入。
 */
public record ActivityPricingRequest(
    Long memberId,
    String requestNo,
    String activityType,
    Long activityId,
    Long recordId,
    Long headId,
    List<Line> lines) {
    public record Line(Long skuId, Integer quantity) {
    }
}

package com.lemnt.promotion.client.bo;

import java.io.Serializable;
import java.util.List;

/**
 * 原订单资源冻结键，禁止在重试时修改资源内容。
 */
public record PromotionLockBo(
    Long orderId,
    String orderNo,
    Long memberId,
    Long couponId,
    String activityType,
    Long activityId,
    Long activityRecordId,
    Long headId,
    List<Line> lines)
    implements Serializable {
    public record Line(Long spuId, Long skuId, int quantity) implements Serializable {
    }
}

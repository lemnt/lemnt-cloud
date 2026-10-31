package com.lemnt.mall.domain.promotion.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 活动订单与交易订单的对应关系，状态变更以条件更新保证幂等。 */
public record PromotionOrder(
        Long id,
        String orderNo,
        String requestNo,
        Long memberId,
        String activityType,
        Long activityId,
        Long activityRecordId,
        Long headId,
        Long skuId,
        Integer quantity,
        BigDecimal activityPrice,
        Long usePoint,
        Integer refundedQuantity,
        String status,
        String failureReason,
        LocalDateTime createTime,
        LocalDateTime updateTime
) implements java.io.Serializable {
}

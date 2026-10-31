package com.lemnt.mall.domain.promotion.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 拼团活动商品，保存活动状态与时间快照。 */
public record CombinationProduct(
        Long id,
        Long activityId,
        Long spuId,
        Long skuId,
        BigDecimal combinationPrice,
        Integer activityStatus,
        LocalDateTime activityStartTime,
        LocalDateTime activityEndTime
) implements java.io.Serializable {
}

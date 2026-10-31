package com.lemnt.mall.domain.promotion.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 砍价记录，状态 1 砍价中、2 成功、3 失败。 */
public record BargainRecord(
        Long id,
        Long userId,
        Long activityId,
        Long spuId,
        Long skuId,
        BigDecimal bargainFirstPrice,
        BigDecimal bargainPrice,
        Integer status,
        LocalDateTime endTime,
        Long orderId
) implements java.io.Serializable {
}

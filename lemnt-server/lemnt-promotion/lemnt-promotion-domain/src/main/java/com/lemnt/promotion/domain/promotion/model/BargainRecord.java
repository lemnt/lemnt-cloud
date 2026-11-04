package com.lemnt.promotion.domain.promotion.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 砍价记录，状态 1 砍价中、2 成功、3 失败；bargainMinPrice 为开砍时冻结底价，达标判定不随活动配置变更。 */
public record BargainRecord(
        Long id,
        Long userId,
        Long activityId,
        Long spuId,
        Long skuId,
        BigDecimal bargainFirstPrice,
        BigDecimal bargainMinPrice,
        BigDecimal bargainPrice,
        Integer status,
        LocalDateTime endTime,
        Long orderId)
        implements Serializable {}

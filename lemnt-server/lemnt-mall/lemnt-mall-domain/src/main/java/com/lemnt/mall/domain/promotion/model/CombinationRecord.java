package com.lemnt.mall.domain.promotion.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 拼团记录，团长与团员通过 headId 关联，团长自身 headId 为 0。 */
public record CombinationRecord(
        Long id,
        Long activityId,
        BigDecimal combinationPrice,
        Long spuId,
        String spuName,
        String picUrl,
        Long skuId,
        Integer count,
        Long userId,
        String nickname,
        String avatar,
        Long headId,
        Integer status,
        Long orderId,
        Integer userSize,
        Integer userCount,
        Boolean virtualGroup,
        LocalDateTime expireTime,
        LocalDateTime startTime,
        LocalDateTime endTime
) implements java.io.Serializable {
    public static final long HEAD_ID_GROUP = 0L;
}

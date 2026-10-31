package com.lemnt.mall.domain.promotion.model;

import java.time.LocalDateTime;

/** 拼团活动。 */
public record CombinationActivity(
        Long id,
        String name,
        Long spuId,
        Integer totalLimitCount,
        Integer singleLimitCount,
        LocalDateTime startTime,
        LocalDateTime endTime,
        Integer userSize,
        Boolean virtualGroup,
        Integer status,
        Integer limitDuration
) implements java.io.Serializable {
}

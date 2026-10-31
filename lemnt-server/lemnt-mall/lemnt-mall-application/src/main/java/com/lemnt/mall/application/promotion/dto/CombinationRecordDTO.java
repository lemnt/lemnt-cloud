package com.lemnt.mall.application.promotion.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 拼团记录查询结果。 */
public record CombinationRecordDTO(
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
) {}

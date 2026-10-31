package com.lemnt.mall.application.promotion.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 砍价记录查询结果。 */
public record BargainRecordDTO(
        Long id,
        Long userId,
        Long activityId,
        Long spuId,
        String spuName,
        String picUrl,
        Long skuId,
        BigDecimal bargainFirstPrice,
        BigDecimal bargainPrice,
        Integer status,
        LocalDateTime endTime,
        Long orderId
) {}

package com.lemnt.mall.application.promotion.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 砍价活动查询结果。 */
public record BargainActivityDTO(
        Long id,
        String name,
        LocalDateTime startTime,
        LocalDateTime endTime,
        Integer status,
        Long spuId,
        String spuName,
        String picUrl,
        BigDecimal skuPrice,
        Long skuId,
        BigDecimal bargainFirstPrice,
        BigDecimal bargainMinPrice,
        Integer stock,
        Integer totalStock,
        Integer helpMaxCount,
        Integer bargainCount,
        Integer totalLimitCount,
        BigDecimal randomMinPrice,
        BigDecimal randomMaxPrice,
        Long successCount
) {}

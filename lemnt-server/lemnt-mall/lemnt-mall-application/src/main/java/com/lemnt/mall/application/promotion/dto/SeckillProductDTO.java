package com.lemnt.mall.application.promotion.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 秒杀活动商品查询结果。 */
public record SeckillProductDTO(
        Long id,
        Long skuId,
        Long spuId,
        BigDecimal seckillPrice,
        Integer stock,
        LocalDateTime activityStartTime,
        LocalDateTime activityEndTime
) {}

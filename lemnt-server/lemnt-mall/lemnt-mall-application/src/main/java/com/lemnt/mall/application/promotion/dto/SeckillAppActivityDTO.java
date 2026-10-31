package com.lemnt.mall.application.promotion.dto;

import java.math.BigDecimal;

/** App 秒杀活动摘要。 */
public record SeckillAppActivityDTO(
        Long id,
        String name,
        Long spuId,
        String spuName,
        String picUrl,
        BigDecimal marketPrice,
        Integer status,
        Integer stock,
        Integer totalStock,
        BigDecimal seckillPrice
) {}

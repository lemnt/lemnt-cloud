package com.lemnt.mall.application.promotion.dto;

import java.math.BigDecimal;

/** 积分商城活动商品查询结果。 */
public record PointProductDTO(
        Long id,
        Long skuId,
        Long spuId,
        Integer exchangeCount,
        Integer point,
        BigDecimal price,
        Integer stock
) {}

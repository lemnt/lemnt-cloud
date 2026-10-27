package com.lemnt.mall.application.catalog.dto;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 商品SKU数据传输对象。
 */
public record SkuDTO(
        Long id,
        Long spuId,
        String code,
        String name,
        BigDecimal price,
        Integer stock,
        String image,
        Map<String,
        String> properties,
        String status
) implements java.io.Serializable {}

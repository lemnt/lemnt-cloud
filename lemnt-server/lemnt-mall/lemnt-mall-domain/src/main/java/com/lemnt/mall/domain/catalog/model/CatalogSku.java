package com.lemnt.mall.domain.catalog.model;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 目录商品SKU。
 */
public record CatalogSku(
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
) {
    /**
     * 创建目录商品SKU实例。
     */
    public CatalogSku {
        properties = properties == null ? Map.of() : Map.copyOf(properties);
    }
}

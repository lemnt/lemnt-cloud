package com.lemnt.mall.domain.product.model;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 目录商品SKU。
 */
public record ProductSku(
        Long id,
        Long spuId,
        String code,
        String name,
        BigDecimal price,
        Integer stock,
        String image,
        Map<String,
        String> properties,
        String status,
        BigDecimal weight,
        BigDecimal volume
) {
    /**
     * 创建目录商品SKU实例。
     */
    public ProductSku {
        properties = properties == null ? Map.of() : Map.copyOf(properties);
    }

    /** 历史快照缺少配送字段，读取时保留未配置状态。 */
    public ProductSku(
            Long id,
            Long spuId,
            String code,
            String name,
            BigDecimal price,
            Integer stock,
            String image,
            Map<String, String> properties,
            String status) {
        this(id, spuId, code, name, price, stock, image, properties, status, null, null);
    }
}

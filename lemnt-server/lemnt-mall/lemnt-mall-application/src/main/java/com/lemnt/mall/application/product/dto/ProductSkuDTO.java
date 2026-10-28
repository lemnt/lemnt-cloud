package com.lemnt.mall.application.product.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;

/**
 * 商品SKU数据传输对象。
 */
public record ProductSkuDTO(
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
) implements Serializable {
    /** 历史快照缺少配送字段，读取时保留未配置状态。 */
    public ProductSkuDTO(
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

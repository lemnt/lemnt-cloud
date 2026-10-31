package com.lemnt.mall.application.product.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

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
        List<Property> properties,
        String status,
        BigDecimal weight,
        BigDecimal volume
) implements Serializable {
    /** 缺少配送字段的规格列表。 */
    public ProductSkuDTO(Long id, Long spuId, String code, String name, BigDecimal price, Integer stock,
            String image, List<Property> properties, String status) {
        this(id, spuId, code, name, price, stock, image, properties, status, null, null);
    }
    /** 编号与名称保持一一对应。 */
    public record Property(
            Long propertyId,
            String propertyName,
            Long valueId,
            String valueName
    ) implements java.io.Serializable {}
}

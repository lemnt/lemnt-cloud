package com.lemnt.mall.application.product.dto;

import java.util.List;

/**
 * 门店商品SKU数据传输对象。
 */
public record StoreSkuDTO(
        Long id,
        String name,
        String price,
        int stock,
        String image,
        List<Property> properties
) implements java.io.Serializable {
    /** 编号与名称保持一一对应。 */
    public record Property(
            Long propertyId,
            String propertyName,
            Long valueId,
            String valueName
    ) implements java.io.Serializable {}
}

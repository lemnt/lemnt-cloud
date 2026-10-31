package com.lemnt.mall.application.product.dto;

import java.io.Serializable;
import java.math.BigDecimal;

/** 商品基础接口应用结果。 */
public record ProductDTO(
        Long id,
        String productCode,
        String name,
        BigDecimal price,
        Integer stock,
        String status
) implements Serializable {
}

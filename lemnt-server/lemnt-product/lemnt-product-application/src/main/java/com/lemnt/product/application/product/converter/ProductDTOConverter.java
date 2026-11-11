package com.lemnt.product.application.product.converter;

import com.lemnt.product.application.product.dto.ProductDTO;
import com.lemnt.product.domain.product.model.Product;

/**
 * 商品基础应用结果转换。
 */
public final class ProductDTOConverter {
    private ProductDTOConverter() {
    }

    /**
     * 领域模型不进入HTTP层。
     */
    public static ProductDTO toDTO(Product value) {
        return value == null
            ? null
            : new ProductDTO(
            value.id(),
            value.productCode(),
            value.name(),
            value.price(),
            value.stock(),
            value.status());
    }
}

package com.lemnt.mall.application.product.converter;

import com.lemnt.mall.application.product.dto.ProductDTO;
import com.lemnt.mall.domain.product.model.Product;

/** 商品基础应用结果转换。 */
public final class ProductDTOConverter {
    private ProductDTOConverter() {
    }

    /** 领域模型不进入HTTP层。 */
    public static ProductDTO toDTO(Product value) {
        return value == null ? null : new ProductDTO(value.id(), value.productCode(), value.name(), value.price(),
            value.stock(), value.status());
    }
}

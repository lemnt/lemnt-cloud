package com.lemnt.product.domain.product.model;

import java.math.BigDecimal;

/**
 * 门店商品。
 */
public record StoreProduct(
        Long id,
        String name,
        Long categoryId,
        Long brandId,
        String image,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        boolean available,
        Integer salesCount,
        Long displaySalesCount) {
    /** 兼容旧调用，新增字段保持未知。 */
    public StoreProduct(
            Long id,
            String name,
            Long categoryId,
            Long brandId,
            String image,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            boolean available,
            Integer salesCount) {
        this(id, name, categoryId, brandId, image, minPrice, maxPrice, available, salesCount, null);
    }
}

package com.lemnt.mall.domain.store.model;

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
        boolean available
) {}

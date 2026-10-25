package com.lemnt.mall.domain.store.model;

public record StoreProduct(
        Long id,
        String name,
        Long categoryId,
        Long brandId,
        String image,
        java.math.BigDecimal minPrice,
        java.math.BigDecimal maxPrice,
        boolean available) {}

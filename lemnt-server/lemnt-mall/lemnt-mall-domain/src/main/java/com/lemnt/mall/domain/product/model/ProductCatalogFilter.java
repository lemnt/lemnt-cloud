package com.lemnt.mall.domain.product.model;

/**
 * 目录筛选。
 */
public record ProductCatalogFilter(
        String name,
        String status,
        int offset,
        int limit,
        Long categoryId,
        Long brandId,
        ProductStockStatus stockStatus
) {}

package com.lemnt.mall.domain.catalog.model;

import java.util.List;

/**
 * 目录商品SPU。
 */
public record CatalogSpu(
        Long id,
        String code,
        String name,
        Long categoryId,
        Long brandId,
        String description,
        String image,
        String status,
        List<CatalogSku> skus
) {
    /**
     * 创建目录商品SPU实例。
     */
    public CatalogSpu {
        skus = skus == null ? List.of() : List.copyOf(skus);
    }
}

package com.lemnt.mall.domain.catalog.model;

public record CatalogSpu(
        Long id,
        String code,
        String name,
        Long categoryId,
        Long brandId,
        String description,
        String image,
        String status,
        java.util.List<CatalogSku> skus) {
    public CatalogSpu {
        skus = skus == null ? java.util.List.of() : java.util.List.copyOf(skus);
    }
}

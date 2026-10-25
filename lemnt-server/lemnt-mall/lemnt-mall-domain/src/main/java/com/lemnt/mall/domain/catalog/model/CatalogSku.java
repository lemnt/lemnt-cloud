package com.lemnt.mall.domain.catalog.model;

public record CatalogSku(
        Long id,
        Long spuId,
        String code,
        String name,
        java.math.BigDecimal price,
        Integer stock,
        String image,
        java.util.Map<String, String> properties,
        String status) {
    public CatalogSku {
        properties = properties == null ? java.util.Map.of() : java.util.Map.copyOf(properties);
    }
}

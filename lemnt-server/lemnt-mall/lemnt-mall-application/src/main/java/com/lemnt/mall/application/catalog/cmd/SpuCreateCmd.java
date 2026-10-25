package com.lemnt.mall.application.catalog.cmd;

public record SpuCreateCmd(
        String code,
        String name,
        Long categoryId,
        Long brandId,
        String description,
        String image,
        java.util.List<SkuDraft> skus) {
    public record SkuDraft(
            String code,
            String name,
            java.math.BigDecimal price,
            Integer stock,
            String image,
            java.util.Map<String, String> properties) {}
}

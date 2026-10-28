package com.lemnt.mall.domain.product.model;

import java.util.List;

/**
 * 目录商品SPU。
 */
public record ProductSpu(
        Long id,
        String code,
        String name,
        Long categoryId,
        Long brandId,
        String description,
        String image,
        String status,
        List<ProductSku> skus,
        List<Integer> deliveryTypes,
        Long deliveryTemplateId
) {
    /**
     * 创建目录商品SPU实例。
     */
    public ProductSpu {
        deliveryTypes = deliveryTypes == null ? List.of() : List.copyOf(deliveryTypes);
        skus = skus == null ? List.of() : List.copyOf(skus);
    }

    /** 历史快照缺少配送字段，读取时保留未配置状态。 */
    public ProductSpu(
            Long id,
            String code,
            String name,
            Long categoryId,
            Long brandId,
            String description,
            String image,
            String status,
            List<ProductSku> skus) {
        this(id, code, name, categoryId, brandId, description, image, status, skus, List.of(), null);
    }
}

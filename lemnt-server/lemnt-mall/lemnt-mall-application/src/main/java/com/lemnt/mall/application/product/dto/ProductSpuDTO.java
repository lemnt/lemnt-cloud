package com.lemnt.mall.application.product.dto;

import java.io.Serializable;
import java.util.List;

/**
 * 商品SPU数据传输对象。
 */
public record ProductSpuDTO(
        Long id,
        String code,
        String name,
        Long categoryId,
        Long brandId,
        String description,
        String image,
        String status,
        List<ProductSkuDTO> skus,
        List<Integer> deliveryTypes,
        Long deliveryTemplateId
) implements Serializable {
    /** 历史快照缺少配送字段，读取时保留未配置状态。 */
    public ProductSpuDTO(
            Long id,
            String code,
            String name,
            Long categoryId,
            Long brandId,
            String description,
            String image,
            String status,
            List<ProductSkuDTO> skus) {
        this(id, code, name, categoryId, brandId, description, image, status, skus, List.of(), null);
    }
}

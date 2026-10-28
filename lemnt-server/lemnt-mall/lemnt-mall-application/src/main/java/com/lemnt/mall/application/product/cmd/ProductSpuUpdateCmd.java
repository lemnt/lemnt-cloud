package com.lemnt.mall.application.product.cmd;

import com.lemnt.mall.domain.product.model.ProductSku;

import java.io.Serializable;
import java.util.List;

/**
 * 商品SPU更新命令。
 */
public record ProductSpuUpdateCmd(
        Long id,
        String name,
        Long categoryId,
        Long brandId,
        String description,
        String image,
        List<ProductSku> skus,
        List<Integer> deliveryTypes,
        Long deliveryTemplateId
) implements Serializable {
    /** 历史快照缺少配送字段，读取时保留未配置状态。 */
    public ProductSpuUpdateCmd(
            Long id,
            String name,
            Long categoryId,
            Long brandId,
            String description,
            String image,
            List<ProductSku> skus) {
        this(id, name, categoryId, brandId, description, image, skus, List.of(), null);
    }
}

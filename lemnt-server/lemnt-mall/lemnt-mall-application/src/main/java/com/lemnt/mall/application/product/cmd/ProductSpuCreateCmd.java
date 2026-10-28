package com.lemnt.mall.application.product.cmd;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 商品SPU创建命令。
 */
public record ProductSpuCreateCmd(
        String code,
        String name,
        Long categoryId,
        Long brandId,
        String description,
        String image,
        List<ProductSkuDraft> skus,
        List<Integer> deliveryTypes,
        Long deliveryTemplateId
) implements Serializable {
    /**
     * 商品商品SKU草稿。
     */
    public record ProductSkuDraft(
            String code,
            String name,
            BigDecimal price,
            Integer stock,
            String image,
            Map<String, String> properties,
            BigDecimal weight,
            BigDecimal volume
    ) {
        /** 旧商品草稿尚未配置重量和体积。 */
        public ProductSkuDraft(String code, String name, BigDecimal price, Integer stock, String image,
                Map<String, String> properties) {
            this(code, name, price, stock, image, properties, null, null);
        }
    }

    /** 历史快照缺少配送字段，读取时保留未配置状态。 */
    public ProductSpuCreateCmd(
            String code,
            String name,
            Long categoryId,
            Long brandId,
            String description,
            String image,
            List<ProductSkuDraft> skus) {
        this(code, name, categoryId, brandId, description, image, skus, List.of(), null);
    }
}

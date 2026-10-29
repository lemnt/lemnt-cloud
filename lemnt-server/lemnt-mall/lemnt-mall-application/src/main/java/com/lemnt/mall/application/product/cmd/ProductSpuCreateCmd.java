package com.lemnt.mall.application.product.cmd;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

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
    public record ProductSkuDraft(String code, String name, BigDecimal price, Integer stock, String image,
            List<Property> properties, BigDecimal weight, BigDecimal volume) {
        /** 未配置配送参数的草稿。 */
        public ProductSkuDraft(String code, String name, BigDecimal price, Integer stock, String image,
                List<Property> properties) {
            this(code, name, price, stock, image, properties, null, null);
        }
        public record Property(Long propertyId, String propertyName, Long valueId, String valueName) {}
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

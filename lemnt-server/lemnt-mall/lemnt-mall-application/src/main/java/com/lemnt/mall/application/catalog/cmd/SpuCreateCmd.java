package com.lemnt.mall.application.catalog.cmd;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 商品SPU创建命令。
 */
public record SpuCreateCmd(
        String code,
        String name,
        Long categoryId,
        Long brandId,
        String description,
        String image,
        List<SkuDraft> skus
) implements java.io.Serializable {
    /**
     * 商品商品SKU草稿。
     */
    public record SkuDraft(
            String code,
            String name,
            BigDecimal price,
            Integer stock,
            String image,
            Map<String,
            String> properties
    ) {}
}

package com.lemnt.product.application.product.cmd;

import com.lemnt.product.domain.product.model.ProductSku;

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
        Long deliveryTemplateId,
        String keyword,
        String introduction,
        List<String> sliderPicUrls,
        Integer sort,
        Integer virtualSalesCount,
        Boolean specType,
        Integer giveIntegral,
        Boolean subCommissionType)
        implements Serializable {
    /** 兼容旧调用，新增字段保持未知。 */
    public ProductSpuUpdateCmd(
            Long id,
            String name,
            Long categoryId,
            Long brandId,
            String description,
            String image,
            List<ProductSku> skus,
            List<Integer> deliveryTypes,
            Long deliveryTemplateId,
            String keyword,
            String introduction,
            List<String> sliderPicUrls,
            Integer sort) {
        this(
                id,
                name,
                categoryId,
                brandId,
                description,
                image,
                skus,
                deliveryTypes,
                deliveryTemplateId,
                keyword,
                introduction,
                sliderPicUrls,
                sort,
                null,
                null,
                null,
                null);
    }

    /**
     * 历史快照缺少配送字段，读取时保留未配置状态。
     */
    public ProductSpuUpdateCmd(
            Long id,
            String name,
            Long categoryId,
            Long brandId,
            String description,
            String image,
            List<ProductSku> skus) {
        this(
                id,
                name,
                categoryId,
                brandId,
                description,
                image,
                skus,
                List.of(),
                null,
                null,
                null,
                null,
                null);
    }
}

package com.lemnt.product.application.product.cmd;

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
    public ProductSpuCreateCmd(
            String code,
            String name,
            Long categoryId,
            Long brandId,
            String description,
            String image,
            List<ProductSkuDraft> skus,
            List<Integer> deliveryTypes,
            Long deliveryTemplateId,
            String keyword,
            String introduction,
            List<String> sliderPicUrls,
            Integer sort) {
        this(
                code,
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
     * 商品商品SKU草稿。
     */
    public record ProductSkuDraft(
            String code,
            String name,
            BigDecimal price,
            Integer stock,
            String image,
            List<Spec> specs,
            BigDecimal weight,
            BigDecimal volume,
            BigDecimal marketPrice,
            BigDecimal costPrice,
            String barCode,
            BigDecimal firstBrokeragePrice,
            BigDecimal secondBrokeragePrice) {
        /** 旧调用没有商品补充资料，保留未知值。 */
        public ProductSkuDraft(
                String code,
                String name,
                BigDecimal price,
                Integer stock,
                String image,
                List<Spec> specs,
                BigDecimal weight,
                BigDecimal volume) {
            this(
                    code, name, price, stock, image, specs, weight, volume, null, null, null, null,
                    null);
        }

        /**
         * 未配置配送参数的草稿。
         */
        public ProductSkuDraft(
                String code,
                String name,
                BigDecimal price,
                Integer stock,
                String image,
                List<Spec> specs) {
            this(code, name, price, stock, image, specs, null, null);
        }

        public record Spec(Long specId, String specName, Long specValueId, String specValueName) {}
    }

    /**
     * 历史快照缺少配送字段，读取时保留未配置状态。
     */
    public ProductSpuCreateCmd(
            String code,
            String name,
            Long categoryId,
            Long brandId,
            String description,
            String image,
            List<ProductSkuDraft> skus) {
        this(
                code,
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

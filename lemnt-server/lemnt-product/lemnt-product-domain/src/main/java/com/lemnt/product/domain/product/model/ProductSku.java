package com.lemnt.product.domain.product.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 商品SKU及原规格行，编号和名称一起传递。
 */
public record ProductSku(
        Long id,
        Long spuId,
        String code,
        String name,
        BigDecimal price,
        Integer stock,
        String image,
        List<ProductSkuSpec> specs,
        String status,
        BigDecimal weight,
        BigDecimal volume,
        BigDecimal marketPrice,
        BigDecimal costPrice,
        String barCode,
        Long salesCount,
        BigDecimal firstBrokeragePrice,
        BigDecimal secondBrokeragePrice) {
    /** 旧调用没有商品补充资料，保留未知值。 */
    public ProductSku(
            Long id,
            Long spuId,
            String code,
            String name,
            BigDecimal price,
            Integer stock,
            String image,
            List<ProductSkuSpec> specs,
            String status,
            BigDecimal weight,
            BigDecimal volume) {
        this(
                id, spuId, code, name, price, stock, image, specs, status, weight, volume, null,
                null, null, null, null, null);
    }

    public ProductSku {
        specs = specs == null ? List.of() : List.copyOf(specs);
    }

    /** 创建或编辑资料时不输入支付累计销量。 */
    public ProductSku(
            Long id,
            Long spuId,
            String code,
            String name,
            BigDecimal price,
            Integer stock,
            String image,
            List<ProductSkuSpec> specs,
            String status,
            BigDecimal weight,
            BigDecimal volume,
            BigDecimal marketPrice,
            BigDecimal costPrice,
            String barCode) {
        this(
                id,
                spuId,
                code,
                name,
                price,
                stock,
                image,
                specs,
                status,
                weight,
                volume,
                marketPrice,
                costPrice,
                barCode,
                null,
                null,
                null);
    }

    /**
     * 存量名值转换不推断字典编号。
     */
    public ProductSku(
            Long id,
            Long spuId,
            String code,
            String name,
            BigDecimal price,
            Integer stock,
            String image,
            Map<String, String> specs,
            String status,
            BigDecimal weight,
            BigDecimal volume) {
        this(id, spuId, code, name, price, stock, image, legacy(specs), status, weight, volume);
    }

    /**
     * 兼容缺少配送参数的存量名值调用。
     */
    public ProductSku(
            Long id,
            Long spuId,
            String code,
            String name,
            BigDecimal price,
            Integer stock,
            String image,
            Map<String, String> specs,
            String status) {
        this(id, spuId, code, name, price, stock, image, specs, status, null, null);
    }

    /**
     * 兼容尚未配置重量、体积的规格列表。
     */
    public ProductSku(
            Long id,
            Long spuId,
            String code,
            String name,
            BigDecimal price,
            Integer stock,
            String image,
            List<ProductSkuSpec> specs,
            String status) {
        this(id, spuId, code, name, price, stock, image, specs, status, null, null);
    }

    private static List<ProductSkuSpec> legacy(Map<String, String> values) {
        return values == null
                ? List.of()
                : values.entrySet().stream()
                        .sorted(Map.Entry.comparingByKey())
                        .map(
                                entry ->
                                        new ProductSkuSpec(
                                                null, entry.getKey(), null, entry.getValue()))
                        .toList();
    }
}

package com.lemnt.product.application.product.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 商品SKU数据传输对象。
 */
public record ProductSkuDTO(
        Long id,
        Long spuId,
        String code,
        String name,
        BigDecimal price,
        Integer stock,
        String image,
        List<Spec> specs,
        String status,
        BigDecimal weight,
        BigDecimal volume,
        BigDecimal marketPrice,
        BigDecimal costPrice,
        String barCode,
        Long salesCount,
        BigDecimal firstBrokeragePrice,
        BigDecimal secondBrokeragePrice)
        implements Serializable {
    /** 旧调用没有商品补充资料，保留未知值。 */
    public ProductSkuDTO(
            Long id,
            Long spuId,
            String code,
            String name,
            BigDecimal price,
            Integer stock,
            String image,
            List<Spec> specs,
            String status,
            BigDecimal weight,
            BigDecimal volume) {
        this(
                id, spuId, code, name, price, stock, image, specs, status, weight, volume, null,
                null, null, null, null, null);
    }

    /**
     * 缺少配送字段的规格列表。
     */
    public ProductSkuDTO(
            Long id,
            Long spuId,
            String code,
            String name,
            BigDecimal price,
            Integer stock,
            String image,
            List<Spec> specs,
            String status) {
        this(id, spuId, code, name, price, stock, image, specs, status, null, null);
    }

    /**
     * 编号与名称保持一一对应。
     */
    public record Spec(Long specId, String specName, Long specValueId, String specValueName)
            implements Serializable {}
}

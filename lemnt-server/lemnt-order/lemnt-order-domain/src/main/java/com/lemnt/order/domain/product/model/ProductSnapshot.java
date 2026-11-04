package com.lemnt.order.domain.product.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 商品快照。
 */
public record ProductSnapshot(
    Long id,
    String productCode,
    String name,
    BigDecimal price,
    Integer stock,
    List<Integer> deliveryTypes,
    Long deliveryTemplateId,
    BigDecimal weight,
    BigDecimal volume,
    Long spuId,
    String spuName,
    List<Spec> specs,
    String picUrl) {
    public ProductSnapshot {
        specs = specs == null ? null : List.copyOf(specs);
    }

    /**
     * 历史未保存商品元数据，保持为空，不查询现值补历史。
     */
    public ProductSnapshot(
        Long id,
        String productCode,
        String name,
        BigDecimal price,
        Integer stock,
        List<Integer> deliveryTypes,
        Long deliveryTemplateId,
        BigDecimal weight,
        BigDecimal volume) {
        this(
            id,
            productCode,
            name,
            price,
            stock,
            deliveryTypes,
            deliveryTemplateId,
            weight,
            volume,
            null,
            null,
            null,
            null);
    }

    /**
     * 历史快照缺少配送字段，读取时保留未配置状态。
     */
    public ProductSnapshot(
        Long id, String productCode, String name, BigDecimal price, Integer stock) {
        this(id, productCode, name, price, stock, List.of(), null, null, null);
    }

    /**
     * 保存时的规格编号和名称，历史缺失编号保持为空。
     */
    public record Spec(Long specId, String specName, Long specValueId, String specValueName)
        implements Serializable {
    }
}

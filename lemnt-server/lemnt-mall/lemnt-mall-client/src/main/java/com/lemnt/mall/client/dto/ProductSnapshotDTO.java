package com.lemnt.mall.client.dto;

import java.util.Map;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 商品快照数据传输对象。
 */
public record ProductSnapshotDTO(
        Long id,
        String productCode,
        String name,
        BigDecimal price,
        Integer stock,
        Long spuId,
        String spuName,
        Map<String, String> properties,
        String picUrl
) implements Serializable {
    public ProductSnapshotDTO {
        properties = properties == null ? null : Map.copyOf(properties);
    }

    /** 历史未保存商品元数据，保持为空，不查询现值补历史。 */
    public ProductSnapshotDTO(
            Long id,
            String productCode,
            String name,
            BigDecimal price,
            Integer stock
    ) {
        this(
                id,
                productCode,
                name,
                price,
                stock,
                null,
                null,
                null,
                null);
    }
}

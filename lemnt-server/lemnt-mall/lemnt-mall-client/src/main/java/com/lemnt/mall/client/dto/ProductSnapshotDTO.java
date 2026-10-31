package com.lemnt.mall.client.dto;

import java.util.List;

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
        List<Property> properties,
        String picUrl
) implements Serializable {
    public ProductSnapshotDTO {
        properties = properties == null ? null : List.copyOf(properties);
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

    /** 保存时的规格编号和名称，历史缺失编号保持为空。 */
    public record Property(
            Long propertyId,
            String propertyName,
            Long valueId,
            String valueName
    ) implements java.io.Serializable {}
}

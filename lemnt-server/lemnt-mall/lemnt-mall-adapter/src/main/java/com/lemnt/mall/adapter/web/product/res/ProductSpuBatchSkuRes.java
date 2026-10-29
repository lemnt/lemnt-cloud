package com.lemnt.mall.adapter.web.product.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/** 批量商品详情SKU记录。 */
@Schema(description = "批量商品详情SKU记录")
public record ProductSpuBatchSkuRes(
        @Schema(description = "SKU编号")
        Long id,
        @Schema(description = "SPU编号")
        Long spuId,
        @Schema(description = "SKU编码")
        String code,
        @Schema(description = "SKU名称")
        String name,
        @Schema(description = "单价，单位元")
        BigDecimal price,
        @Schema(description = "可用库存")
        Integer stock,
        @Schema(description = "图片地址")
        String image,
        @Schema(description = "规格快照")
        List<Property> properties,
        @Schema(description = "ON_SHELF或OFF_SHELF")
        String status,
        @Schema(description = "重量，千克")
        BigDecimal weight,
        @Schema(description = "体积，立方米")
        BigDecimal volume
) implements Serializable {
    /** 缺少配送字段的规格列表。 */
    public ProductSpuBatchSkuRes(Long id, Long spuId, String code, String name, BigDecimal price, Integer stock,
            String image, List<Property> properties, String status) {
        this(id, spuId, code, name, price, stock, image, properties, status, null, null);
    }
    /** 编号与名称保持一一对应。 */
    @Schema(description = "商品规格")
    public record Property(
            @Schema(description = "规格项编号")
            Long propertyId,
            @Schema(description = "规格名称，写入时可省略")
            String propertyName,
            @Schema(description = "规格值编号")
            Long valueId,
            @Schema(description = "规格值名称，写入时可省略")
            String valueName
    ) implements java.io.Serializable {}
}

package com.lemnt.mall.adapter.web.product.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 商品SKU响应。
 */
@Schema(description = "Sku响应")
public record ProductSkuRes(
        @Schema(description = "编号")
        Long id,
        @Schema(description = "所属SPU编号，存量独立商品为null")
        Long spuId,
        @Schema(description = "编码")
        String code,
        @Schema(description = "名称")
        String name,
        @Schema(description = "价格，元")
        BigDecimal price,
        @Schema(description = "可用库存")
        Integer stock,
        @Schema(description = "图片URL")
        String image,
        @Schema(description = "SKU规格列表")
        List<Property> properties,
        @Schema(description = "状态")
        String status,
        @Schema(description = "重量，千克")
        BigDecimal weight,
        @Schema(description = "体积，立方米")
        BigDecimal volume
) implements Serializable {
    /** 缺少配送字段的规格列表。 */
    public ProductSkuRes(Long id, Long spuId, String code, String name, BigDecimal price, Integer stock,
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

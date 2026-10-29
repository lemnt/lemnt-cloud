package com.lemnt.mall.adapter.web.product.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/** SKU新增响应。 */
@Schema(description = "SKU新增响应")
public record ProductSkuAddRes(
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
        String status
) implements Serializable {
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

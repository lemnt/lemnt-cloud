package com.lemnt.mall.adapter.web.product.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.math.BigDecimal;

/** SKU分页响应。 */
@Schema(description = "SKU分页响应")
public record ProductSkuPageRes(
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
        @Schema(description = "ON_SHELF或OFF_SHELF")
        String status
) implements Serializable {
}

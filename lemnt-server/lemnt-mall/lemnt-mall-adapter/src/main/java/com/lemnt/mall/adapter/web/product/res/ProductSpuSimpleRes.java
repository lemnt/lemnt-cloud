package com.lemnt.mall.adapter.web.product.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

/** 上架商品选择记录。 */
@Schema(description = "上架商品选择记录")
public record ProductSpuSimpleRes(
        @Schema(description = "商品ID")
        Long id,
        @Schema(description = "商品编码")
        String code,
        @Schema(description = "商品名称")
        String name
) implements Serializable {
}

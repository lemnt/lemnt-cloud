package com.lemnt.mall.adapter.web.product.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

/** 商品状态统计响应。 */
@Schema(description = "商品状态统计响应")
public record ProductSpuCountRes(
        @Schema(description = "上架数量")
        long onShelf,
        @Schema(description = "下架数量")
        long offShelf,
        @Schema(description = "回收站数量")
        long recycled
) implements Serializable {
}

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
        long recycled,
        @Schema(description = "总库存为0的商品数，包含未删除的回收站商品")
        long soldOut,
        @Schema(description = "库存不超过10且不在回收站的商品数")
        long lowStock
) implements Serializable {
}

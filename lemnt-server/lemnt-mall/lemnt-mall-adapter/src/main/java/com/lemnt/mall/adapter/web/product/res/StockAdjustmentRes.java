package com.lemnt.mall.adapter.web.product.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

/**
 * 库存调整响应。
 */
@Schema(description = "StockAdjustment响应")
public record StockAdjustmentRes(
        @Schema(description = "库存请求编号")
        String requestNo,
        @Schema(description = "SKU编号")
        Long productId,
        @Schema(description = "库存增减量")
        int delta,
        @Schema(description = "变更原因")
        String reason,
        @Schema(description = "该请求执行后的库存快照")
        Integer resultingStock
) implements Serializable {}

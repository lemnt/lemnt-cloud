package com.lemnt.mall.adapter.web.product.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.time.LocalDateTime;

/** 库存调整历史记录。 */
@Schema(description = "库存调整历史记录")
public record StockAdjustmentPageRes(
        @Schema(description = "原调整请求编号")
        String requestNo,
        @Schema(description = "SKU编号")
        Long productId,
        @Schema(description = "库存增减数量")
        int delta,
        @Schema(description = "调整原因")
        String reason,
        @Schema(description = "当次完成后的库存")
        Integer resultingStock,
        @Schema(description = "调整时间")
        LocalDateTime createTime
) implements Serializable {
}

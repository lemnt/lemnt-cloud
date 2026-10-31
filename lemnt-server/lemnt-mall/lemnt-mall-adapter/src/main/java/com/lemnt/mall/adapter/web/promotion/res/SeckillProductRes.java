package com.lemnt.mall.adapter.web.promotion.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 秒杀活动商品响应。 */
@Schema(description = "秒杀活动商品响应")
public record SeckillProductRes(
        @Schema(description = "商品明细编号")
        Long id,
        @Schema(description = "SKU 编号")
        Long skuId,
        @Schema(description = "SPU 编号")
        Long spuId,
        @Schema(description = "秒杀价")
        BigDecimal seckillPrice,
        @Schema(description = "剩余库存")
        Integer stock,
        @Schema(description = "活动开始时间")
        LocalDateTime activityStartTime,
        @Schema(description = "活动结束时间")
        LocalDateTime activityEndTime
) implements java.io.Serializable {
}

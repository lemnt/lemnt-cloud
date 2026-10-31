package com.lemnt.mall.adapter.app.promotion.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

/** App 秒杀活动商品。 */
@Schema(description = "App 秒杀活动商品")
public record AppSeckillProductRes(
        @Schema(description = "SKU 编号")
        Long skuId,
        @Schema(description = "秒杀价")
        BigDecimal seckillPrice,
        @Schema(description = "剩余库存")
        Integer stock
) implements java.io.Serializable {
}

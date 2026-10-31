package com.lemnt.mall.adapter.app.promotion.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

/** App 秒杀活动摘要。 */
@Schema(description = "App 秒杀活动摘要")
public record AppSeckillActivityRes(
        @Schema(description = "活动编号")
        Long id,
        @Schema(description = "活动名称")
        String name,
        @Schema(description = "商品 SPU 编号")
        Long spuId,
        @Schema(description = "商品名称")
        String spuName,
        @Schema(description = "商品主图")
        String picUrl,
        @Schema(description = "商品市场价")
        BigDecimal marketPrice,
        @Schema(description = "0 开启，1 关闭")
        Integer status,
        @Schema(description = "剩余活动库存")
        Integer stock,
        @Schema(description = "活动总库存")
        Integer totalStock,
        @Schema(description = "最低秒杀价")
        BigDecimal seckillPrice
) implements java.io.Serializable {
}

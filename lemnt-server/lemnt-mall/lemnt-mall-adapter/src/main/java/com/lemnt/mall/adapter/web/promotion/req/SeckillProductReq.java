package com.lemnt.mall.adapter.web.promotion.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/** 秒杀活动商品请求。 */
@Schema(description = "秒杀活动商品请求")
public record SeckillProductReq(
        @Schema(description = "SKU 编号")
        @NotNull
        Long skuId,
        @Schema(description = "秒杀价，单位元，最多两位小数")
        @NotNull
        BigDecimal seckillPrice,
        @Schema(description = "秒杀库存")
        @NotNull
        Integer stock
) implements java.io.Serializable {
}

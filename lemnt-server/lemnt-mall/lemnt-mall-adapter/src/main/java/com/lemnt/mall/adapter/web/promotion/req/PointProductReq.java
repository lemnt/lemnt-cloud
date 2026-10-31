package com.lemnt.mall.adapter.web.promotion.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/** 积分商城活动商品请求。 */
@Schema(description = "积分商城活动商品请求")
public record PointProductReq(
        @Schema(description = "SKU 编号")
        @NotNull
        Long skuId,
        @Schema(description = "单会员可兑换次数")
        @NotNull
        Integer exchangeCount,
        @Schema(description = "所需积分")
        @NotNull
        Integer point,
        @Schema(description = "补差金额，单位元")
        @NotNull
        BigDecimal price,
        @Schema(description = "库存")
        @NotNull
        Integer stock
) implements java.io.Serializable {
}

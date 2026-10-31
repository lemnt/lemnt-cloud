package com.lemnt.mall.adapter.web.promotion.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/** 拼团活动商品请求。 */
@Schema(description = "拼团活动商品请求")
public record CombinationProductReq(
        @Schema(description = "SKU 编号")
        @NotNull
        Long skuId,
        @Schema(description = "拼团价，单位元，最多两位小数")
        @NotNull
        BigDecimal combinationPrice
) implements java.io.Serializable {
}

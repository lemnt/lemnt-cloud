package com.lemnt.mall.adapter.web.promotion.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

/** 拼团活动商品响应。 */
@Schema(description = "拼团活动商品响应")
public record CombinationProductRes(
        @Schema(description = "SKU 编号")
        Long skuId,
        @Schema(description = "拼团价")
        BigDecimal combinationPrice
) implements java.io.Serializable {
}

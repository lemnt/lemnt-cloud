package com.lemnt.mall.adapter.web.promotion.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

/** 折扣商品响应。 */
@Schema(description = "折扣商品响应")
public record DiscountProductRes(
        @Schema(description = "商品 SPU 编号")
        Long spuId,
        @Schema(description = "商品 SKU 编号")
        Long skuId,
        @Schema(description = "PRICE 减价或 PERCENT 打折")
        String discountType,
        @Schema(description = "折扣基点，8000 表示八折")
        Integer discountPercent,
        @Schema(description = "优惠金额，单位元")
        BigDecimal discountPrice
) implements java.io.Serializable {
}

package com.lemnt.mall.adapter.web.promotion.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

/** 折扣商品请求。 */
@Schema(description = "折扣商品请求")
public record DiscountProductReq(
        @Schema(description = "商品 SPU 编号")
        @NotNull @Positive
        Long spuId,
        @Schema(description = "商品 SKU 编号")
        @NotNull @Positive
        Long skuId,
        @Schema(description = "PRICE 减价或 PERCENT 打折")
        @NotBlank
        @Pattern(regexp = "PRICE|PERCENT")
        String discountType,
        @Schema(description = "折扣基点，8000 表示八折")
        Integer discountPercent,
        @Schema(description = "优惠金额，单位元")
        BigDecimal discountPrice
) implements java.io.Serializable {
}

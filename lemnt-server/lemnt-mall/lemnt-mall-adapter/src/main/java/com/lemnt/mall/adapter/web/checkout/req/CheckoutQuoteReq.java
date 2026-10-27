package com.lemnt.mall.adapter.web.checkout.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

/**
 * 结算报价请求。
 */
@Schema(description = "CheckoutQuoteReq")
public record CheckoutQuoteReq(
        @Schema(description = "该会员购物车中已选中的SKU编号，1至100项")
        @NotEmpty
        @Size(max = 100)
        List<@NotNull
        @Positive
        Long> productIds,
        @Schema(description = "该会员的优惠券实例编号，可不传")
        @Positive
        Long couponId
) implements Serializable {}

package com.lemnt.trade.adapter.web.checkout.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;

@Schema(description = "CheckoutQuoteReq")
public record CheckoutQuoteReq(
        @Schema(description = "该会员购物车中已选中的SKU编号，1至100项") @NotEmpty @Size(max = 100)
                java.util.List<@NotNull @Positive Long> productIds,
        @Schema(description = "该会员的优惠券实例编号，可不传") @Positive Long couponId)
        implements Serializable {}

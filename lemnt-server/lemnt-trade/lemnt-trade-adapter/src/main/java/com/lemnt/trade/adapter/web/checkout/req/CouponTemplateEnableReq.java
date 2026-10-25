package com.lemnt.trade.adapter.web.checkout.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;

@Schema(description = "CouponTemplateEnableReq")
public record CouponTemplateEnableReq(
        @Schema(description = "启用新发放及新兑换，已锁定订单不受影响") @NotNull Boolean enabled)
        implements Serializable {}

package com.lemnt.mall.adapter.web.checkout.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 优惠券模板启用请求。
 */
@Schema(description = "CouponTemplateEnableReq")
public record CouponTemplateEnableReq(
        @Schema(description = "启用新发放及新兑换，已锁定订单不受影响")
        @NotNull
        Boolean enabled
) implements Serializable {}

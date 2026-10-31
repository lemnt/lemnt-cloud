package com.lemnt.mall.adapter.web.coupon.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

/**
 * 优惠券公开领取请求。
 */
@Schema(description = "CouponPublicClaimReq请求")
public record CouponPublicClaimReq(
        @Schema(description = "是否允许会员自行领取，默认关闭")
        @NotNull
        Boolean enabled
) implements Serializable {}

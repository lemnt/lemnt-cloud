package com.lemnt.mall.adapter.app.coupon.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

/**
 * 优惠券可领取优惠响应。
 */
@Schema(description = "MallAppCouponCenterOffersRes响应")
public record MallAppCouponCenterOffersRes(
        @Schema(description = "优惠券规则")
        CouponTemplateRes template,
        @Schema(description = "本人持有券ID，未领取为null")
        Long couponId,
        @Schema(description = "本人券状态，未领取为null")
        String couponStatus,
        @Schema(description = "服务端核验当前可用，不保证结算时仍有效")
        boolean usable,
        @Schema(description = "当前可领取")
        boolean claimable
) implements Serializable {}

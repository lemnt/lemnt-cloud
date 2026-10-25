package com.lemnt.trade.adapter.web.store.res;

@io.swagger.v3.oas.annotations.media.Schema(description = "CouponOfferRes响应")
public record CouponOfferRes(
        @io.swagger.v3.oas.annotations.media.Schema(description = "优惠券规则")
                com.lemnt.trade.adapter.web.checkout.res.CouponTemplateRes template,
        @io.swagger.v3.oas.annotations.media.Schema(description = "本人持有券ID，未领取为null") Long couponId,
        @io.swagger.v3.oas.annotations.media.Schema(description = "本人券状态，未领取为null")
                String couponStatus,
        @io.swagger.v3.oas.annotations.media.Schema(description = "服务端核验当前可用，不保证结算时仍有效")
                boolean usable,
        @io.swagger.v3.oas.annotations.media.Schema(description = "当前可领取") boolean claimable)
        implements java.io.Serializable {}

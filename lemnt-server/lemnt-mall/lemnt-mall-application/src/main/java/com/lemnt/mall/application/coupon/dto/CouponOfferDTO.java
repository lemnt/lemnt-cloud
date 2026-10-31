package com.lemnt.mall.application.coupon.dto;

import java.io.Serializable;

/**
 * 优惠券可领取优惠数据传输对象。
 */
public record CouponOfferDTO(
        CouponTemplateDTO template,
        Long couponId,
        String couponStatus,
        boolean usable,
        boolean claimable
) implements Serializable {}

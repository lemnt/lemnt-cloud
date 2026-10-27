package com.lemnt.mall.application.store.dto;

import com.lemnt.mall.application.checkout.dto.CouponTemplateDTO;

/**
 * 优惠券可领取优惠数据传输对象。
 */
public record CouponOfferDTO(
        CouponTemplateDTO template,
        Long couponId,
        String couponStatus,
        boolean usable,
        boolean claimable
) implements java.io.Serializable {}

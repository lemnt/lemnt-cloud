package com.lemnt.trade.application.store.dto;

public record CouponOfferDTO(
        com.lemnt.trade.application.checkout.dto.CouponTemplateDTO template,
        Long couponId,
        String couponStatus,
        boolean usable,
        boolean claimable) {}

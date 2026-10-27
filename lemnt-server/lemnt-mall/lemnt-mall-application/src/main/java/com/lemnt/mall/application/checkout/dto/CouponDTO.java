package com.lemnt.mall.application.checkout.dto;

/**
 * 优惠券数据传输对象。
 */
public record CouponDTO(
        Long id,
        Long templateId,
        Long memberId,
        String status,
        String orderNo
) implements java.io.Serializable {}

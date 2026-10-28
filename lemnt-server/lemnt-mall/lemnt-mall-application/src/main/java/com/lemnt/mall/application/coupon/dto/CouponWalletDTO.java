package com.lemnt.mall.application.coupon.dto;

import java.io.Serializable;

/** 会员券包只返回发放时冻结的权益。 */
public record CouponWalletDTO(Long couponId, Long templateId, String couponStatus,
        CouponTermsDTO terms, boolean usable) implements Serializable {
}

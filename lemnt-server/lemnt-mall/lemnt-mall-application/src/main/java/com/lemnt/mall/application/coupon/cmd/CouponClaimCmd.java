package com.lemnt.mall.application.coupon.cmd;

import java.io.Serializable;

/**
 * 优惠券领取命令。
 */
public record CouponClaimCmd(
        Long memberId,
        Long templateId
) implements Serializable {}

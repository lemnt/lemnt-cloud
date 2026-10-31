package com.lemnt.mall.application.coupon.cmd;

import java.io.Serializable;

/**
 * 优惠券公开领取命令。
 */
public record CouponPublicClaimCmd(
        Long templateId,
        boolean enabled
) implements Serializable {}

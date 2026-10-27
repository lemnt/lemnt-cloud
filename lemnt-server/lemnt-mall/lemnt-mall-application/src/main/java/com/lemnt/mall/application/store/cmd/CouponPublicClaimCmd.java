package com.lemnt.mall.application.store.cmd;

/**
 * 优惠券公开领取命令。
 */
public record CouponPublicClaimCmd(
        Long templateId,
        boolean enabled
) implements java.io.Serializable {}

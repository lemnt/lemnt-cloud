package com.lemnt.mall.application.store.cmd;

/**
 * 优惠券领取命令。
 */
public record CouponClaimCmd(
        Long memberId,
        Long templateId
) implements java.io.Serializable {}

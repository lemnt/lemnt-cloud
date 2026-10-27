package com.lemnt.mall.application.checkout.cmd;

/**
 * 优惠券签发命令。
 */
public record CouponIssueCmd(
        Long memberId,
        Long templateId
) implements java.io.Serializable {}

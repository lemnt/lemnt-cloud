package com.lemnt.mall.application.coupon.cmd;

import java.io.Serializable;

/**
 * 优惠券签发命令。
 */
public record CouponIssueCmd(
        Long memberId,
        Long templateId
) implements Serializable {}

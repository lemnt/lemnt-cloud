package com.lemnt.promotion.application.coupon.cmd;

import java.io.Serializable;

/**
 * 优惠券模板启用命令。
 */
public record CouponTemplateEnableCmd(Long id, boolean enabled) implements Serializable {}

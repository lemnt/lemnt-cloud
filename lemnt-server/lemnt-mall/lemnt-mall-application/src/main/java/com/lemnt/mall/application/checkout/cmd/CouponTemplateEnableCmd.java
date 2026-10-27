package com.lemnt.mall.application.checkout.cmd;

/**
 * 优惠券模板启用命令。
 */
public record CouponTemplateEnableCmd(
        Long id,
        boolean enabled
) implements java.io.Serializable {}

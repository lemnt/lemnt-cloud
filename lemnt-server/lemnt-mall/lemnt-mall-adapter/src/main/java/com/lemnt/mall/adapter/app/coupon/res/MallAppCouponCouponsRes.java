package com.lemnt.mall.adapter.app.coupon.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

/**
 * 优惠券响应。
 */
@Schema(description = "Coupon响应")
public record MallAppCouponCouponsRes(
        @Schema(description = "编号")
        Long id,
        @Schema(description = "优惠券模板编号")
        Long templateId,
        @Schema(description = "后台授权操作的目标会员编号，不是当前后台用户ID")
        Long memberId,
        @Schema(description = "订单RESERVING/PENDING_PAYMENT/PAID/CLOSED，优惠券AVAILABLE/LOCKED/USED")
        String status,
        @Schema(description = "订单号")
        String orderNo
) implements Serializable {}

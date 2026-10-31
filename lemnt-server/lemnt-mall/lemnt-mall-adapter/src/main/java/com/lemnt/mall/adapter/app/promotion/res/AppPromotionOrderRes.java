package com.lemnt.mall.adapter.app.promotion.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

/** App 活动下单响应。 */
@Schema(description = "App 活动下单响应")
public record AppPromotionOrderRes(
        @Schema(description = "订单号")
        String orderNo,
        @Schema(description = "实付金额")
        BigDecimal totalAmount,
        @Schema(description = "订单状态")
        String status,
        @Schema(description = "消耗积分")
        Long usePoint,
        @Schema(description = "赠送积分")
        Long givePoint
) implements java.io.Serializable {
}

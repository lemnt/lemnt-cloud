package com.lemnt.trade.adapter.app.order.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

/** 会员订单状态数量。 */
@Schema(description = "会员订单状态数量")
public record AppOrderStatusCountRes(
        @Schema(description = "待付款")
        long pendingPayment,
        @Schema(description = "待发货")
        long awaitingShipment,
        @Schema(description = "待收货")
        long awaitingReceipt,
        @Schema(description = "待评价")
        long awaitingComment,
        @Schema(description = "已完成")
        long completed,
        @Schema(description = "已取消")
        long closed,
        @Schema(description = "已退款")
        long refunded
) implements Serializable {
}

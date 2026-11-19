package com.lemnt.order.adapter.web.order.res;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "交易待办数量")
public record OrderWorkloadRes(
        @Schema(description = "已支付待发货订单数") long shipments,
        @Schema(description = "已支付待自提订单数") long pickups,
        @Schema(description = "待审核售后申请数") long afterSales,
        @Schema(description = "待审核佣金提现数") long withdrawals) {}

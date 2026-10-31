package com.lemnt.trade.adapter.app.order.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** 会员订单详情。 */
@Schema(description = "会员订单详情")
public record AppOrderDetailRes(
        @Schema(description = "订单编号")
        String orderNo,
        @Schema(description = "订单状态")
        String status,
        @Schema(description = "应付金额，元")
        BigDecimal totalAmount,
        @Schema(description = "下单时间")
        LocalDateTime createTime,
        @Schema(description = "是否有处理中售后")
        boolean activeRefund,
        @Schema(description = "商品明细")
        List<AppOrderItemRes> items,
        @Schema(description = "物流信息")
        AppOrderShipmentRes shipment,
        @Schema(description = "是否全部评价")
        Boolean commentStatus,
        @Schema(description = "全部评价完成时间")
        LocalDateTime finishTime
) implements Serializable {
    public AppOrderDetailRes {
        items = items == null ? List.of() : List.copyOf(items);
    }
}

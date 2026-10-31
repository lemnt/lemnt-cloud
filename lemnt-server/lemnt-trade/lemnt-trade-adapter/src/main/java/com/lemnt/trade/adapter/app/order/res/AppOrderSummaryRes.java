package com.lemnt.trade.adapter.app.order.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** 会员订单列表项。 */
@Schema(description = "会员订单列表项")
public record AppOrderSummaryRes(
        @Schema(description = "订单编号")
        String orderNo,
        @Schema(description = "订单状态")
        String status,
        @Schema(description = "应付金额，元")
        BigDecimal totalAmount,
        @Schema(description = "下单时间")
        LocalDateTime createTime,
        @Schema(description = "是否全部评价，历史未知为空")
        Boolean commentStatus,
        @Schema(description = "全部评价完成时间")
        LocalDateTime finishTime,
        @Schema(description = "是否有处理中售后")
        boolean activeRefund,
        @Schema(description = "商品明细")
        List<AppOrderItemRes> items
) implements Serializable {
    public AppOrderSummaryRes {
        items = items == null ? List.of() : List.copyOf(items);
    }
}

package com.lemnt.trade.adapter.web.order.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/** 订单详情响应。 */
@Schema(description = "订单详情响应")
public record TradeOrderDetailRes(
        @Schema(description = "订单ID")
        Long id,
        @Schema(description = "订单编号")
        String orderNo,
        @Schema(description = "会员ID")
        Long memberId,
        @Schema(description = "订单金额，单位元")
        BigDecimal totalAmount,
        @Schema(description = "订单状态")
        String status,
        @Schema(description = "订单商品快照")
        List<TradeOrderItemRes> items
) implements Serializable {
}

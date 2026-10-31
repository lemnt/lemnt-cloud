package com.lemnt.trade.adapter.web.order.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.math.BigDecimal;

/** 订单统计结果。 */
@Schema(description = "订单统计结果")
public record OrderStatisticsRes(
        @Schema(description = "订单数量")
        long orderCount,
        @Schema(description = "订单应付金额合计，单位元，非净收入")
        BigDecimal orderPayPrice,
        @Schema(description = "成功支付退款笔数")
        long afterSaleCount,
        @Schema(description = "成功支付退款金额合计，单位元")
        BigDecimal afterSalePrice
) implements Serializable {
}

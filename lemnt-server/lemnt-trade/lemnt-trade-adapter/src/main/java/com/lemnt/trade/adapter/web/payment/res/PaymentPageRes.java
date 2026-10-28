package com.lemnt.trade.adapter.web.payment.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.math.BigDecimal;

/** 后台支付单分页记录。 */
@Schema(description = "后台支付单分页记录")
public record PaymentPageRes(
        @Schema(description = "支付单ID")
        Long id,
        @Schema(description = "支付单编号")
        String paymentNo,
        @Schema(description = "订单编号")
        String orderNo,
        @Schema(description = "原支付渠道编码")
        String channel,
        @Schema(description = "渠道支付交易编号")
        String channelTradeNo,
        @Schema(description = "支付金额，单位元")
        BigDecimal amount,
        @Schema(description = "支付状态：WAITING、SUCCESS、CLOSED")
        String status
) implements Serializable {
}

package com.lemnt.pay.adapter.web.payment.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 后台支付单详情。
 */
@Schema(description = "后台支付单详情")
public record PaymentDetailRes(
        @Schema(description = "支付单ID") Long id,
        @Schema(description = "支付单编号") String paymentNo,
        @Schema(description = "订单编号") String orderNo,
        @Schema(description = "原支付渠道编码") String channel,
        @Schema(description = "渠道支付交易编号") String channelTradeNo,
        @Schema(description = "支付金额，单位元") BigDecimal amount,
        @Schema(description = "支付状态：WAITING、SUCCESS、CLOSED") String status,
        @Schema(description = "支付成功时渠道费率，单位百分比") BigDecimal channelFeeRate,
        @Schema(description = "按费率计算的渠道手续费，单位元") BigDecimal channelFeeAmount,
        @Schema(description = "支付主单编号") String paymentOrderNo,
        @Schema(description = "主单状态，RECONCILE_REQUIRED 表示待对账") String paymentOrderStatus)
        implements Serializable {}

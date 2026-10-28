package com.lemnt.trade.adapter.web.paymentrefund.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.math.BigDecimal;

/** 后台支付退款发起结果。 */
@Schema(description = "后台支付退款发起结果")
public record PaymentRefundCreateRes(
        @Schema(description = "支付退款交易编号")
        String refundNo,
        @Schema(description = "关联售后单编号")
        String afterSaleNo,
        @Schema(description = "原支付单编号")
        String paymentNo,
        @Schema(description = "订单编号")
        String orderNo,
        @Schema(description = "原路退款渠道编码")
        String channel,
        @Schema(description = "退款金额，单位元")
        BigDecimal amount,
        @Schema(description = "退款状态：WAITING、SUCCESS、FAILED")
        String status,
        @Schema(description = "渠道退款交易编号，未受理时为空")
        String channelRefundNo
) implements Serializable {
}

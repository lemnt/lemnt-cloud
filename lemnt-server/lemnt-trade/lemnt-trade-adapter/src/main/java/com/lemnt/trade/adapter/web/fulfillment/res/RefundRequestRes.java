package com.lemnt.trade.adapter.web.fulfillment.res;

import java.io.Serializable;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "订单履约RefundRequestRes")
public record RefundRequestRes(
        @Schema(description = "退款申请编号") String refundNo,
        @Schema(description = "订单编号") String orderNo,
        @Schema(description = "幂等请求编号") String requestNo,
        @Schema(description = "会员ID") Long memberId,
        @Schema(description = "成功支付编号") String paymentNo,
        @Schema(description = "整单申请金额，人民币元；不是已退款金额") java.math.BigDecimal amount,
        @Schema(description = "申请原因") String reason,
        @Schema(description = "申请状态：REQUESTED待处理（不是已退款），REJECTED已驳回") String status,
        @Schema(description = "驳回原因") String decisionReason,
        @Schema(description = "申请时间") java.time.LocalDateTime requestedAt,
        @Schema(description = "驳回时间") java.time.LocalDateTime decidedAt)
        implements Serializable {}

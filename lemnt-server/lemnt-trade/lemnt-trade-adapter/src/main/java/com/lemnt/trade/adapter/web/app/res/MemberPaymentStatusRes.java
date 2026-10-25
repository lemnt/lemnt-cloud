package com.lemnt.trade.adapter.web.app.res;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "本人订单本地支付状态，不代表真实渠道查询结果")
public record MemberPaymentStatusRes(
        @Schema(description = "订单编号") String orderNo,
        @Schema(description = "本地订单状态") String orderStatus,
        @Schema(description = "应付金额，人民币元") java.math.BigDecimal payableAmount,
        @Schema(description = "本地支付状态：NOT_CREATED/WAITING/SUCCESS/CLOSED/RECONCILE_REQUIRED")
                String localPaymentStatus,
        @Schema(description = "是否核验真实渠道；当前固定false，不能用于安全关单") boolean channelVerified)
        implements java.io.Serializable {}

package com.lemnt.trade.adapter.web.recharge.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 充值单响应。 */
@Schema(description = "充值单响应")
public record WalletRechargeRes(
        @Schema(description = "充值单编号")
        Long id,
        @Schema(description = "充值单号")
        String rechargeNo,
        @Schema(description = "请求编号")
        String requestNo,
        @Schema(description = "钱包编号")
        Long walletId,
        @Schema(description = "会员编号")
        Long memberId,
        @Schema(description = "套餐编号")
        Long packageId,
        @Schema(description = "支付金额，元")
        BigDecimal payPrice,
        @Schema(description = "赠送金额，元")
        BigDecimal bonusPrice,
        @Schema(description = "到账金额，元")
        BigDecimal totalPrice,
        @Schema(description = "状态")
        String status,
        @Schema(description = "渠道编码")
        String channelCode,
        @Schema(description = "到账时间")
        LocalDateTime paidTime,
        @Schema(description = "退款时间")
        LocalDateTime refundedTime,
        @Schema(description = "创建时间")
        LocalDateTime createTime
) implements Serializable {
}

package com.lemnt.trade.adapter.app.payment.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

/** 小程序可选支付渠道。 */
@Schema(description = "小程序可选支付渠道")
public record AppPaymentChannelRes(
        @Schema(description = "渠道编码")
        String code,
        @Schema(description = "渠道名称")
        String name
) implements Serializable {
}

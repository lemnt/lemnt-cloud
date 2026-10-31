package com.lemnt.trade.adapter.web.payment.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

/** 支持的支付渠道。 */
@Schema(description = "支持的支付渠道")
public record PaymentChannelRes(
        @Schema(description = "支付渠道编码：ALIPAY、WECHAT")
        String code,
        @Schema(description = "支付渠道名称")
        String name
) implements Serializable {
}

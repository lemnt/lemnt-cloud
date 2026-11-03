package com.lemnt.pay.adapter.app.payment.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/** 会员发起订单支付请求。 */
@Schema(description = "会员发起订单支付请求")
public record AppPaymentCreateReq(
        @Schema(description = "支付应用标识", example = "lemnt-mall")
                @NotBlank(message = "支付应用不能为空")
                @Size(max = 64)
                String appKey,
        @Schema(description = "支付渠道：ALIPAY/WECHAT/SANDBOX", example = "ALIPAY")
                @NotBlank(message = "支付渠道不能为空")
                @Size(max = 32)
                String channel)
        implements Serializable {}

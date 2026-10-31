package com.lemnt.trade.adapter.web.recharge.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Size;

import java.io.Serializable;

/** 确认充值到账请求。 */
@Schema(description = "确认充值到账请求")
public record WalletRechargeConfirmReq(
        @Schema(description = "渠道编码：ALIPAY/WECHAT，可空")
        @Size(max = 32) String channelCode
) implements Serializable {
}

package com.lemnt.pay.adapter.web.recharge.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

@Schema(description = "钱包充值统计")
public record WalletRechargeSummaryRes(
        @Schema(description = "累计充值实付，元；不含赠送，退款前毛额") String rechargePrice)
        implements Serializable {}

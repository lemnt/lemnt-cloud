package com.lemnt.trade.adapter.app.brokerage.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/** 会员佣金提现申请请求。 */
@Schema(description = "会员佣金提现申请请求")
public record AppBrokerageWithdrawCreateReq(
        @Schema(description = "提现金额，元") @NotNull BigDecimal price,
        @Schema(description = "提现方式：1钱包、2银行卡、3微信收款码、4支付宝收款码") @NotNull Integer type,
        @Schema(description = "收款人姓名") @Size(max = 64) String userName,
        @Schema(description = "银行卡号") @Size(max = 128) String userAccount,
        @Schema(description = "收款码地址") @Size(max = 500) String qrCodeUrl,
        @Schema(description = "银行名称") @Size(max = 100) String bankName,
        @Schema(description = "开户地址") @Size(max = 255) String bankAddress)
        implements java.io.Serializable {}

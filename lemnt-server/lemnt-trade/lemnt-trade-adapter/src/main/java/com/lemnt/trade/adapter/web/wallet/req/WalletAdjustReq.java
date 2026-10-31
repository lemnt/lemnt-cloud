package com.lemnt.trade.adapter.web.wallet.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.math.BigDecimal;

/** 人工调整钱包余额请求，amount 正增负减。 */
@Schema(description = "钱包余额调整请求")
public record WalletAdjustReq(
        @Schema(description = "幂等请求编号")
        @NotBlank @Size(max = 64) String requestNo,
        @Schema(description = "调整金额，正增负减，元")
        @NotNull @Digits(integer = 9, fraction = 2) BigDecimal amount,
        @Schema(description = "调整原因")
        @NotBlank @Size(max = 128) String reason
) implements Serializable {
}

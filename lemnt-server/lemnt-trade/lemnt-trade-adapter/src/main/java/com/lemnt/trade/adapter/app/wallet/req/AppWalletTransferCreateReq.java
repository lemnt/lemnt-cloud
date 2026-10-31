package com.lemnt.trade.adapter.app.wallet.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.math.BigDecimal;

/** App 本人钱包转账请求。 */
@Schema(description = "钱包转账请求")
public record AppWalletTransferCreateReq(
        @Schema(description = "转入会员编号")
        @NotNull @Positive Long toMemberId,
        @Schema(description = "转账金额，元")
        @NotNull @DecimalMin("0.01") @Digits(integer = 9, fraction = 2) BigDecimal amount,
        @Schema(description = "备注")
        @Size(max = 128) String remark,
        @Schema(description = "幂等请求编号")
        @NotBlank @Size(max = 64) String requestNo
) implements Serializable {
}

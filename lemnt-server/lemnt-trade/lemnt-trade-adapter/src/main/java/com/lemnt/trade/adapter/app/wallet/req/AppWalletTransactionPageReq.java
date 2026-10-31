package com.lemnt.trade.adapter.app.wallet.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

/** App 本人钱包流水分页请求。 */
@Schema(description = "本人钱包流水分页请求")
public record AppWalletTransactionPageReq(
        @Schema(description = "业务类型")
        @Size(max = 32) String bizType,
        @Schema(description = "页码默认1")
        @Min(1) @Max(1000000) Integer pageNum,
        @Schema(description = "每页数量默认10")
        @Min(1) @Max(100) Integer pageSize
) implements Serializable {
}

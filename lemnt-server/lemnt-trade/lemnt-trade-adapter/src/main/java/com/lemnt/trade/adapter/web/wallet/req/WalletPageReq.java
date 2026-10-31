package com.lemnt.trade.adapter.web.wallet.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

import java.io.Serializable;

/** 钱包分页请求。 */
@Schema(description = "钱包分页请求")
public record WalletPageReq(
        @Schema(description = "会员编号")
        @Positive Long memberId,
        @Schema(description = "页码默认1")
        @Min(1) @Max(1000000) Integer pageNum,
        @Schema(description = "每页数量默认20")
        @Min(1) @Max(100) Integer pageSize
) implements Serializable {
}

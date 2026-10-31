package com.lemnt.trade.adapter.web.wallet.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

/** 钱包转账分页请求。 */
@Schema(description = "钱包转账分页请求")
public record WalletTransferPageReq(
        @Schema(description = "转出会员编号")
        @Positive Long fromMemberId,
        @Schema(description = "转入会员编号")
        @Positive Long toMemberId,
        @Schema(description = "状态，当前仅 SUCCESS")
        @Size(max = 20) String status,
        @Schema(description = "页码默认1")
        @Min(1) @Max(1000000) Integer pageNum,
        @Schema(description = "每页数量默认20")
        @Min(1) @Max(100) Integer pageSize
) implements Serializable {
}

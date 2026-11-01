package com.lemnt.trade.adapter.web.brokerage.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** 提现审核请求。 */
@Schema(description = "提现审核请求")
public record BrokerageWithdrawAuditReq(
        @Schema(description = "是否通过") @NotNull Boolean approved,
        @Schema(description = "审核说明") @Size(max = 255) String reason
) implements java.io.Serializable {
}

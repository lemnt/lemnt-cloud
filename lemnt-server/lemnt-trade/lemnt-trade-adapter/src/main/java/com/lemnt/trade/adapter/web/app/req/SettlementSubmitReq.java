package com.lemnt.trade.adapter.web.app.req;

import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "从服务端缓存结算单提单")
public record SettlementSubmitReq(
        @Schema(description = "服务端结算单编号") @NotBlank @Pattern(regexp = "[a-f0-9-]{36}")
                String settlementId,
        @Schema(description = "幂等提单请求编号") @NotBlank @Size(max = 64) String requestNo)
        implements java.io.Serializable {}

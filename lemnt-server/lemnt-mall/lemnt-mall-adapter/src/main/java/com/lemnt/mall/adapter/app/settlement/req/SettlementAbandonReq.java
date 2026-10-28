package com.lemnt.mall.adapter.app.settlement.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/**
 * 从服务端缓存结算单提单。
 */
@Schema(description = "从服务端缓存结算单提单")
public record SettlementAbandonReq(
        @Schema(description = "服务端结算单编号")
        @NotBlank
        @Pattern(regexp = "[a-f0-9-]{36}")
        String settlementId,
        @Schema(description = "幂等提单请求编号")
        @NotBlank
        @Size(max = 64)
        String requestNo
) implements Serializable {}

package com.lemnt.member.adapter.web.growth.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/**
 * 资产调整请求。
 */
@Schema(description = "AssetAdjust请求")
public record MemberUserPointsReq(
        @Schema(description = "本次有符号变更量")
        @NotNull
        Long delta,
        @Schema(description = "幂等业务编号，重试不可更改内容")
        @NotBlank
        @Size(max = 64)
        String requestNo,
        @Schema(description = "原因")
        @NotBlank
        @Size(max = 500)
        String reason
) implements Serializable {}

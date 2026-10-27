package com.lemnt.member.adapter.web.growth.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/**
 * 等级分配请求。
 */
@Schema(description = "LevelAssign请求")
public record LevelAssignReq(
        @Schema(description = "等级编号，可空")
        Long levelId,
        @Schema(description = "幂等业务编号，重试不可更改内容")
        @NotBlank
        @Size(max = 64)
        String requestNo,
        @Schema(description = "原因")
        @NotBlank
        @Size(max = 500)
        String reason
) implements Serializable {}

package com.lemnt.member.adapter.web.growth.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 签到配置保存请求。
 */
@Schema(description = "SignConfigUpdate请求")
public record SignConfigUpdateReq(
        @Schema(description = "本轮连续签到第几天")
        @NotNull
        Integer day,
        @Schema(description = "奖励积分")
        @NotNull
        Long point,
        @Schema(description = "奖励经验")
        @NotNull
        Long experience,
        @Schema(description = "状态：1启用，0停用")
        @NotNull
        Integer status
) implements Serializable {}

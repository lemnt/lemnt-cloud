package com.lemnt.member.adapter.web.growth.res;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;

/**
 * 签到配置响应。
 */
@Schema(description = "会员SignConfig响应")
public record MemberConfigDetailRes(
        @Schema(description = "本轮连续签到第几天")
        Integer day,
        @Schema(description = "奖励积分")
        Long point,
        @Schema(description = "经验值或等级升级门槛")
        Long experience,
        @Schema(description = "状态：1启用，0停用")
        Integer status
) implements Serializable {}

package com.lemnt.member.adapter.web.growth.res;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 签到记录响应。
 */
@Schema(description = "会员SignRecord响应")
public record MemberUserSignsRes(
        @Schema(description = "记录编号")
        Long id,
        @Schema(description = "会员编号")
        Long memberId,
        @Schema(description = "签到业务日期")
        LocalDate signDate,
        @Schema(description = "本轮连续签到第几天")
        Integer day,
        @Schema(description = "奖励积分")
        Long point,
        @Schema(description = "经验值或等级升级门槛")
        Long experience
) implements Serializable {}

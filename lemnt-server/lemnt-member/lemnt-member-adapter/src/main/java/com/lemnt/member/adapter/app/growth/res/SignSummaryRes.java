package com.lemnt.member.adapter.app.growth.res;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;

/**
 * 签到汇总，连续天数按配置周期重置。
 */
@Schema(description = "签到汇总，连续天数按配置周期重置")
public record SignSummaryRes(
        @Schema(description = "累计签到天数")
        long totalDay,
        @Schema(description = "当前周期连续天数")
        int continuousDay,
        @Schema(description = "今日是否签到")
        boolean todaySignIn
) implements Serializable {}

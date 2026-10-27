package com.lemnt.member.application.growth.dto;

import java.io.Serializable;

/**
 * 签到汇总数据传输对象。
 */
public record SignSummaryDTO(
        long totalDay,
        int continuousDay,
        boolean todaySignIn
) implements Serializable {}

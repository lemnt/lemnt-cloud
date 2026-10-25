package com.lemnt.member.adapter.web.account.res;

@io.swagger.v3.oas.annotations.media.Schema(description = "签到汇总，连续天数按配置周期重置")
public record SignSummaryRes(
        @io.swagger.v3.oas.annotations.media.Schema(description = "累计签到天数") long totalDay,
        @io.swagger.v3.oas.annotations.media.Schema(description = "当前周期连续天数") int continuousDay,
        @io.swagger.v3.oas.annotations.media.Schema(description = "今日是否签到") boolean todaySignIn)
        implements java.io.Serializable {}

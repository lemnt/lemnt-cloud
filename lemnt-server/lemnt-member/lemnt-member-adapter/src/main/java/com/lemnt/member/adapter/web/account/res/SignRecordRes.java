package com.lemnt.member.adapter.web.account.res;

@io.swagger.v3.oas.annotations.media.Schema(description = "会员SignRecord响应")
public record SignRecordRes(
        @io.swagger.v3.oas.annotations.media.Schema(description = "记录编号") Long id,
        @io.swagger.v3.oas.annotations.media.Schema(description = "会员编号") Long memberId,
        @io.swagger.v3.oas.annotations.media.Schema(description = "签到业务日期")
                java.time.LocalDate signDate,
        @io.swagger.v3.oas.annotations.media.Schema(description = "本轮连续签到第几天") Integer day,
        @io.swagger.v3.oas.annotations.media.Schema(description = "奖励积分") Long point,
        @io.swagger.v3.oas.annotations.media.Schema(description = "经验值或等级升级门槛") Long experience)
        implements java.io.Serializable {}

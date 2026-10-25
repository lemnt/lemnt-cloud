package com.lemnt.member.adapter.web.account.res;

@io.swagger.v3.oas.annotations.media.Schema(description = "会员SignConfig响应")
public record SignConfigRes(
        @io.swagger.v3.oas.annotations.media.Schema(description = "本轮连续签到第几天") Integer day,
        @io.swagger.v3.oas.annotations.media.Schema(description = "奖励积分") Long point,
        @io.swagger.v3.oas.annotations.media.Schema(description = "经验值或等级升级门槛") Long experience,
        @io.swagger.v3.oas.annotations.media.Schema(description = "状态：1启用，0停用") Integer status)
        implements java.io.Serializable {}

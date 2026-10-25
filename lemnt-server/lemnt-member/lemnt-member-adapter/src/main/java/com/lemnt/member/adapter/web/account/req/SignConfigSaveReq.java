package com.lemnt.member.adapter.web.account.req;

@io.swagger.v3.oas.annotations.media.Schema(description = "SignConfigSave请求")
public record SignConfigSaveReq(
        @io.swagger.v3.oas.annotations.media.Schema(description = "本轮连续签到第几天")
                @jakarta.validation.constraints.NotNull
                Integer day,
        @io.swagger.v3.oas.annotations.media.Schema(description = "奖励积分")
                @jakarta.validation.constraints.NotNull
                Long point,
        @io.swagger.v3.oas.annotations.media.Schema(description = "经验值或等级升级门槛")
                @jakarta.validation.constraints.NotNull
                Long experience,
        @io.swagger.v3.oas.annotations.media.Schema(description = "状态：1启用，0停用")
                @jakarta.validation.constraints.NotNull
                Integer status)
        implements java.io.Serializable {}

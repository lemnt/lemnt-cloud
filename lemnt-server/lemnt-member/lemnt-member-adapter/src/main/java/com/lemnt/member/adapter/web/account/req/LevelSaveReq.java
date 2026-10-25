package com.lemnt.member.adapter.web.account.req;

@io.swagger.v3.oas.annotations.media.Schema(description = "LevelSave请求")
public record LevelSaveReq(
        @io.swagger.v3.oas.annotations.media.Schema(description = "名称或收件人")
                @jakarta.validation.constraints.NotBlank
                @jakarta.validation.constraints.Size(max = 80)
                String name,
        @io.swagger.v3.oas.annotations.media.Schema(description = "等级序号")
                @jakarta.validation.constraints.NotNull
                Integer level,
        @io.swagger.v3.oas.annotations.media.Schema(description = "经验值或等级升级门槛")
                @jakarta.validation.constraints.NotNull
                Long experience,
        @io.swagger.v3.oas.annotations.media.Schema(description = "支付百分比，98为98折")
                @jakarta.validation.constraints.NotNull
                Integer discountPercent,
        @io.swagger.v3.oas.annotations.media.Schema(description = "等级图标URL")
                @jakarta.validation.constraints.Size(max = 500)
                String icon,
        @io.swagger.v3.oas.annotations.media.Schema(description = "等级背景URL")
                @jakarta.validation.constraints.Size(max = 500)
                String backgroundUrl,
        @io.swagger.v3.oas.annotations.media.Schema(description = "状态：1启用，0停用")
                @jakarta.validation.constraints.NotNull
                Integer status)
        implements java.io.Serializable {}

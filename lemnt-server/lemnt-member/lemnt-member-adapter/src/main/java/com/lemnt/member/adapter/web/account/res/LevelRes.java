package com.lemnt.member.adapter.web.account.res;

@io.swagger.v3.oas.annotations.media.Schema(description = "会员Level响应")
public record LevelRes(
        @io.swagger.v3.oas.annotations.media.Schema(description = "记录编号") Long id,
        @io.swagger.v3.oas.annotations.media.Schema(description = "名称或收件人") String name,
        @io.swagger.v3.oas.annotations.media.Schema(description = "等级序号") Integer level,
        @io.swagger.v3.oas.annotations.media.Schema(description = "经验值或等级升级门槛") Long experience,
        @io.swagger.v3.oas.annotations.media.Schema(description = "支付百分比，98为98折")
                Integer discountPercent,
        @io.swagger.v3.oas.annotations.media.Schema(description = "等级图标URL") String icon,
        @io.swagger.v3.oas.annotations.media.Schema(description = "等级背景URL") String backgroundUrl,
        @io.swagger.v3.oas.annotations.media.Schema(description = "状态：1启用，0停用") Integer status)
        implements java.io.Serializable {}

package com.lemnt.member.adapter.web.growth.res;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;

/**
 * 等级响应。
 */
@Schema(description = "会员Level响应")
public record MemberLevelListRes(
        @Schema(description = "记录编号")
        Long id,
        @Schema(description = "名称或收件人")
        String name,
        @Schema(description = "等级序号")
        Integer level,
        @Schema(description = "经验值或等级升级门槛")
        Long experience,
        @Schema(description = "支付百分比，98为98折")
        Integer discountPercent,
        @Schema(description = "等级图标URL")
        String icon,
        @Schema(description = "等级背景URL")
        String backgroundUrl,
        @Schema(description = "状态：1启用，0停用")
        Integer status
) implements Serializable {}

package com.lemnt.member.adapter.web.growth.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/**
 * 等级保存请求。
 */
@Schema(description = "LevelSave请求")
public record MemberLevelCreateReq(
        @Schema(description = "名称或收件人")
        @NotBlank
        @Size(max = 80)
        String name,
        @Schema(description = "等级序号")
        @NotNull
        Integer level,
        @Schema(description = "经验值或等级升级门槛")
        @NotNull
        Long experience,
        @Schema(description = "支付百分比，98为98折")
        @NotNull
        Integer discountPercent,
        @Schema(description = "等级图标URL")
        @Size(max = 500)
        String icon,
        @Schema(description = "等级背景URL")
        @Size(max = 500)
        String backgroundUrl,
        @Schema(description = "状态：1启用，0停用")
        @NotNull
        Integer status
) implements Serializable {}

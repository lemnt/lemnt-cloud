package com.lemnt.member.adapter.web.growth.res;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 等级记录响应。
 */
@Schema(description = "会员LevelRecord响应")
public record MemberRecordLevelRecordsDetailRes(
        @Schema(description = "记录编号")
        Long id,
        @Schema(description = "会员编号")
        Long memberId,
        @Schema(description = "等级编号，可空")
        Long levelId,
        @Schema(description = "变更后会员经验")
        Long experience,
        @Schema(description = "原因")
        String reason,
        @Schema(description = "数据库时间")
        LocalDateTime createTime,
        @Schema(description = "变更时等级")
        Integer level,
        @Schema(description = "变更时折扣")
        Integer discountPercent,
        @Schema(description = "源记录经验：自动路径为门槛，手工路径为变更量")
        Long recordExperience,
        @Schema(description = "变更描述快照")
        String description
) implements Serializable {}

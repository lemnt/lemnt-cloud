package com.lemnt.member.adapter.web.growth.res;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 等级记录响应。
 */
@Schema(description = "会员LevelRecord响应")
public record MemberRecordLevelRecordsRes(
        @Schema(description = "记录编号")
        Long id,
        @Schema(description = "会员编号")
        Long memberId,
        @Schema(description = "等级编号，可空")
        Long levelId,
        @Schema(description = "经验值或等级升级门槛")
        Long experience,
        @Schema(description = "原因")
        String reason,
        @Schema(description = "数据库时间")
        LocalDateTime createTime
) implements Serializable {}

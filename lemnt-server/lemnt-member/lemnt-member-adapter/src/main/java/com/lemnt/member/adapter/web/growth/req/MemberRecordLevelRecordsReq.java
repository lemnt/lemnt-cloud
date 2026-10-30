package com.lemnt.member.adapter.web.growth.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/** 后台等级变更记录专用筛选。 */
@Schema(description = "后台等级变更记录分页查询")
public record MemberRecordLevelRecordsReq(
        @Schema(description = "会员编号，未填查询所有会员") @Positive Long memberId,
        @Schema(description = "等级编号精确匹配") @Positive Long levelId,
        @Schema(description = "创建时间起，包含边界")
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
        @Schema(description = "创建时间止，包含边界")
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
        @Schema(description = "页码，默认1") @Min(1) @Max(1000000) Integer pageNum,
        @Schema(description = "页大小，默认20，最大100") @Min(1) @Max(100) Integer pageSize
) implements Serializable {}

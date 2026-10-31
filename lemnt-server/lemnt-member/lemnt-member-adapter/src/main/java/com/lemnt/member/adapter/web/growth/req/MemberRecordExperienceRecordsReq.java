package com.lemnt.member.adapter.web.growth.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/** 后台经验流水分页筛选，各条件对应实际经验表字段。 */
@Schema(description = "后台经验流水分页查询")
public record MemberRecordExperienceRecordsReq(
        @Schema(description = "会员编号，未填查询所有会员") @Positive Long memberId,
        @Schema(description = "业务编号精确匹配") @Size(max = 64) String bizNo,
        @Schema(description = "源经验业务类型，0为管理员调整") Integer experienceBizType,
        @Schema(description = "标题精确匹配") @Size(max = 255) String title,
        @Schema(description = "创建时间起，包含边界")
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
        @Schema(description = "创建时间止，包含边界")
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
        @Schema(description = "页码，默认1") @Min(1) @Max(1000000) Integer pageNum,
        @Schema(description = "页大小，默认20，最大100") @Min(1) @Max(100) Integer pageSize
) implements Serializable {}

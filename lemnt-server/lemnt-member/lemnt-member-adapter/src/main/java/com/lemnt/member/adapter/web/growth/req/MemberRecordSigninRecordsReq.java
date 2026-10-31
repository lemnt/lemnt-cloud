package com.lemnt.member.adapter.web.growth.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * 后台会员分页查询。
 */
@Schema(description = "后台会员分页查询")
public record MemberRecordSigninRecordsReq(
        @Schema(description = "可选会员ID；未填查询所有会员")
        @Positive
        Long memberId,
        @Schema(description = "当前会员昵称包含匹配")
        @Size(max=80)
        String nickname,
        @Schema(description = "连续第几天，精确匹配") @Positive Integer day,
        @Schema(description = "真实签到时间起，包含边界")
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
        @Schema(description = "真实签到时间止，包含边界")
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
        @Schema(description = "页码，默认1")
        @Min(1)
        @Max(1000000)
        Integer pageNum,
        @Schema(description = "页大小，默认20，最大100")
        @Min(1)
        @Max(100)
        Integer pageSize
) implements Serializable {}

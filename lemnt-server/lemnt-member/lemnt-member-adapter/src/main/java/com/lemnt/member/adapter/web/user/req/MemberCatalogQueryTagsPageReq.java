package com.lemnt.member.adapter.web.user.req;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/** 标签独立分页请求，不复用会员查询字段。 */
@Schema(description = "会员标签关联分页查询")
public record MemberCatalogQueryTagsPageReq(
        @Schema(description = "名称包含匹配") @Size(max = 100) String name,
        @Schema(description = "开始时间，yyyy-MM-dd HH:mm:ss")
                @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                LocalDateTime startTime,
        @Schema(description = "结束时间，yyyy-MM-dd HH:mm:ss")
                @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                LocalDateTime endTime,
        @Schema(description = "页码，从1开始") @Min(1) @Max(1000000) Integer pageNum,
        @Schema(description = "每页条数，最多100条") @Min(1) @Max(100) Integer pageSize)
        implements Serializable {}

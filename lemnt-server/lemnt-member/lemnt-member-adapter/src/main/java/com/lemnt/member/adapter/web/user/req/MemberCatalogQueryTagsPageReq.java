package com.lemnt.member.adapter.web.user.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.io.Serializable;
import java.time.LocalDateTime;

/** 标签独立分页请求，不复用会员查询字段。 */
public record MemberCatalogQueryTagsPageReq(
        @Schema(description = "名称包含匹配") @Size(max = 100) String name,
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
        @Min(1) @Max(1000000) Integer pageNum,
        @Min(1) @Max(100) Integer pageSize
) implements Serializable {}

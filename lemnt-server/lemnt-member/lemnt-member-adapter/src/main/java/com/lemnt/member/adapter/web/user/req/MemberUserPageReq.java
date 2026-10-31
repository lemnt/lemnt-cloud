package com.lemnt.member.adapter.web.user.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/** 后台会员列表独立筛选。 */
public record MemberUserPageReq(
        @Schema(description = "手机号包含匹配") @Size(max = 24) String mobile,
        @Schema(description = "邮箱包含匹配") @Size(max = 254) String email,
        @Schema(description = "昵称包含匹配") @Size(max = 80) String nickname,
        @Positive Long levelId,
        @Positive Long groupId,
        @Schema(description = "任一标签命中，空列表不筛选") List<@Positive Long> tagIds,
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime loginStartTime,
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime loginEndTime,
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime createStartTime,
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime createEndTime,
        @Min(1) @Max(1000000) Integer pageNum,
        @Min(1) @Max(100) Integer pageSize
) implements Serializable {}

package com.lemnt.member.adapter.web.user.req;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 后台会员列表独立筛选。
 */
@Schema(description = "会员分页查询")
public record MemberUserPageReq(
    @Schema(description = "手机号包含匹配") @Size(max = 24) String mobile,
    @Schema(description = "邮箱包含匹配") @Size(max = 254) String email,
    @Schema(description = "昵称包含匹配") @Size(max = 80) String nickname,
    @Schema(description = "会员等级编号") @Positive Long levelId,
    @Schema(description = "会员分组编号") @Positive Long groupId,
    @Schema(description = "任一标签命中，空列表不筛选") List<@Positive Long> tagIds,
    @Schema(description = "登录时间下限，yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime loginStartTime,
    @Schema(description = "登录时间上限，yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime loginEndTime,
    @Schema(description = "创建时间下限，yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime createStartTime,
    @Schema(description = "创建时间上限，yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime createEndTime,
    @Schema(description = "页码，从1开始") @Min(1) @Max(1000000) Integer pageNum,
    @Schema(description = "每页条数，最多100条") @Min(1) @Max(100) Integer pageSize)
    implements Serializable {
}

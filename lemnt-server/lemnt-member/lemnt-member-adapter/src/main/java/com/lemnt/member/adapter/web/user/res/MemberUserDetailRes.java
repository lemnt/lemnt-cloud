package com.lemnt.member.adapter.web.user.res;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 资料响应。
 */
@Schema(description = "会员Profile响应")
public record MemberUserDetailRes(
        @Schema(description = "记录编号")
        Long id,
        @Schema(description = "昵称")
        String nickname,
        @Schema(description = "手机号")
        String mobile,
        @Schema(description = "邮箱")
        String email,
        @Schema(description = "头像HTTPS地址")
        String avatar,
        @Schema(description = "状态：1启用，0停用")
        Integer status,
        @Schema(description = "当前积分")
        Long points,
        @Schema(description = "经验值或等级升级门槛")
        Long experience,
        @Schema(description = "等级编号，可空")
        Long levelId,
        @Schema(description = "分组编号，可空")
        Long groupId,
        @Schema(description = "性别：0未知，1男，2女")
        Integer sex,
        @Schema(description = "出生日期")
        LocalDate birthday,
        @Schema(description = "数据库时间")
        LocalDateTime createTime
) implements Serializable {}

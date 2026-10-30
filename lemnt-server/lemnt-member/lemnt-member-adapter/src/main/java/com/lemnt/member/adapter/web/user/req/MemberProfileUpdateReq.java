package com.lemnt.member.adapter.web.user.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 后台资料更新请求。
 */
@Schema(description = "会员资料更新请求")
public record MemberProfileUpdateReq(
        @Schema(description = "昵称")
        @NotBlank
        @Size(max = 80)
        String nickname,
        @Schema(description = "邮箱")
        @Size(max = 254)
        String email,
        @Schema(description = "头像HTTPS地址")
        @Size(max = 500)
        String avatar,
        @Schema(description = "性别：0未知，1男，2女")
        Integer sex,
        @Schema(description = "出生日期")
        LocalDate birthday,
        @Schema(description = "状态：1启用，0停用")
        Integer status,
        @Schema(description = "后台内部备注，省略保留，空串清除")
        @Size(max = 255)
        String mark,
        @Schema(description = "真实姓名，省略保留，空串清除")
        @Size(max = 30)
        String name,
        @Schema(description = "地区编号，省略保留")
        @jakarta.validation.constraints.Positive
        Integer areaId
) implements Serializable {}

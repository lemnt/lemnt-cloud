package com.lemnt.member.adapter.app.user.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 资料更新请求。
 */
@Schema(description = "ProfileUpdate请求")
public record ProfileUpdateReq(
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
        LocalDate birthday
) implements Serializable {}

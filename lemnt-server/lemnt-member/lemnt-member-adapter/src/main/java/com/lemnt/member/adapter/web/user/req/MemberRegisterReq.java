package com.lemnt.member.adapter.web.user.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;

/** 兼容会员注册入口的请求。 */
@Schema(description = "会员注册请求")
public record MemberRegisterReq(
        @Schema(description = "昵称") @NotBlank String nickname,
        @Schema(description = "手机号") String mobile,
        @Schema(description = "邮箱") @Email String email,
        @Schema(description = "头像地址") String avatar
) implements Serializable {}

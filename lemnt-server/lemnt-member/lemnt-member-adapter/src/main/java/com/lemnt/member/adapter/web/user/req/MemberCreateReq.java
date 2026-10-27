package com.lemnt.member.adapter.web.user.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/**
 * 会员创建请求。
 */
@Schema(description = "MemberCreate请求")
public record MemberCreateReq(
        @Schema(description = "昵称")
        @NotBlank
        @Size(max = 80)
        String nickname,
        @Schema(description = "手机号")
        @NotBlank
        @Size(max = 24)
        String mobile,
        @Schema(description = "邮箱")
        @Size(max = 254)
        String email,
        @Schema(description = "头像HTTPS地址")
        @Size(max = 500)
        String avatar
) implements Serializable {}

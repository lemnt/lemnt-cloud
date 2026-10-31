package com.lemnt.auth.adapter.app.member.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/**
 * 认证登录请求。
 */
@Schema(description = "AuthLogin请求")
public record AuthLoginReq(
        @Schema(description = "手机号")
        @NotBlank
        @Size(max = 24)
        String mobile,
        @Schema(description = "密码，至少10字符且最多72 UTF-8字节")
        @NotBlank
        @Size(max = 72)
        String password
) implements Serializable {
    @Override
    public String toString() {
        return "AuthLoginReq[redacted]";
    }
}

package com.lemnt.member.adapter.web.user.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/**
 * 密码重置请求。
 */
@Schema(description = "PasswordReset请求")
public record PasswordResetReq(
        @Schema(description = "密码，至少10字符且最多72 UTF-8字节")
        @NotBlank
        @Size(max = 72)
        String password
) implements Serializable {
    @Override
    public String toString() {
        return "PasswordResetReq[redacted]";
    }
}

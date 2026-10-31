package com.lemnt.member.adapter.app.user.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/**
 * 密码修改请求。
 */
@Schema(description = "PasswordChange请求")
public record PasswordChangeReq(
        @Schema(description = "当前密码")
        @NotBlank
        @Size(max = 72)
        String oldPassword,
        @Schema(description = "新密码，至少10字符且最多72 UTF-8字节")
        @NotBlank
        @Size(max = 72)
        String newPassword
) implements Serializable {
    @Override
    public String toString() {
        return "PasswordChangeReq[redacted]";
    }
}

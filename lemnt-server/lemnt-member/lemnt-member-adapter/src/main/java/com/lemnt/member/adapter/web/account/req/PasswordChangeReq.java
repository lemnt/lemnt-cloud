package com.lemnt.member.adapter.web.account.req;

@io.swagger.v3.oas.annotations.media.Schema(description = "PasswordChange请求")
public record PasswordChangeReq(
        @io.swagger.v3.oas.annotations.media.Schema(description = "当前密码")
                @jakarta.validation.constraints.NotBlank
                @jakarta.validation.constraints.Size(max = 72)
                String oldPassword,
        @io.swagger.v3.oas.annotations.media.Schema(description = "新密码，至少10字符且最多72 UTF-8字节")
                @jakarta.validation.constraints.NotBlank
                @jakarta.validation.constraints.Size(max = 72)
                String newPassword)
        implements java.io.Serializable {
    @Override
    public String toString() {
        return "PasswordChangeReq[redacted]";
    }
}

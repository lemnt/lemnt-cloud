package com.lemnt.member.adapter.web.account.req;

@io.swagger.v3.oas.annotations.media.Schema(description = "AuthLogin请求")
public record AuthLoginReq(
        @io.swagger.v3.oas.annotations.media.Schema(description = "手机号")
                @jakarta.validation.constraints.NotBlank
                @jakarta.validation.constraints.Size(max = 24)
                String mobile,
        @io.swagger.v3.oas.annotations.media.Schema(description = "密码，至少10字符且最多72 UTF-8字节")
                @jakarta.validation.constraints.NotBlank
                @jakarta.validation.constraints.Size(max = 72)
                String password)
        implements java.io.Serializable {
    @Override
    public String toString() {
        return "AuthLoginReq[redacted]";
    }
}

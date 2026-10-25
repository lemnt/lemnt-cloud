package com.lemnt.member.adapter.web.account.req;

@io.swagger.v3.oas.annotations.media.Schema(description = "SocialLogin请求")
public record SocialLoginReq(
        @io.swagger.v3.oas.annotations.media.Schema(description = "provider")
                @jakarta.validation.constraints.NotBlank
                @jakarta.validation.constraints.Size(max = 24)
                String provider,
        @io.swagger.v3.oas.annotations.media.Schema(description = "loginCode")
                @jakarta.validation.constraints.NotBlank
                @jakarta.validation.constraints.Size(max = 256)
                String loginCode,
        @io.swagger.v3.oas.annotations.media.Schema(description = "state")
                @jakarta.validation.constraints.NotBlank
                @jakarta.validation.constraints.Size(max = 64)
                String state)
        implements java.io.Serializable {
    @Override
    public String toString() {
        return "SocialLoginReq[redacted]";
    }
}

package com.lemnt.member.adapter.web.account.req;

@io.swagger.v3.oas.annotations.media.Schema(description = "SocialBind请求")
public record SocialBindReq(
        @io.swagger.v3.oas.annotations.media.Schema(description = "provider")
                @jakarta.validation.constraints.NotBlank
                @jakarta.validation.constraints.Size(max = 24)
                String provider,
        @io.swagger.v3.oas.annotations.media.Schema(description = "code")
                @jakarta.validation.constraints.NotBlank
                @jakarta.validation.constraints.Size(max = 256)
                String code,
        @io.swagger.v3.oas.annotations.media.Schema(description = "state")
                @jakarta.validation.constraints.NotBlank
                @jakarta.validation.constraints.Size(max = 64)
                String state)
        implements java.io.Serializable {
    @Override
    public String toString() {
        return "SocialBindReq[redacted]";
    }
}

package com.lemnt.member.adapter.web.account.req;

@io.swagger.v3.oas.annotations.media.Schema(description = "SocialUnbind请求")
public record SocialUnbindReq(
        @io.swagger.v3.oas.annotations.media.Schema(description = "provider")
                @jakarta.validation.constraints.NotBlank
                @jakarta.validation.constraints.Size(max = 24)
                String provider)
        implements java.io.Serializable {
    @Override
    public String toString() {
        return "SocialUnbindReq[redacted]";
    }
}

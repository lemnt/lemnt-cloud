package com.lemnt.member.adapter.web.account.req;

@io.swagger.v3.oas.annotations.media.Schema(description = "SmsSend请求")
public record SmsSendReq(
        @io.swagger.v3.oas.annotations.media.Schema(description = "mobile")
                @jakarta.validation.constraints.NotBlank
                @jakarta.validation.constraints.Size(max = 24)
                String mobile,
        @io.swagger.v3.oas.annotations.media.Schema(description = "scene")
                @jakarta.validation.constraints.NotBlank
                @jakarta.validation.constraints.Size(max = 40)
                String scene)
        implements java.io.Serializable {
    @Override
    public String toString() {
        return "SmsSendReq[redacted]";
    }
}

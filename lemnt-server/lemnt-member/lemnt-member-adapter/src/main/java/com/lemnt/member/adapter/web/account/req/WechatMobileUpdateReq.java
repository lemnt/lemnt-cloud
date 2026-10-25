package com.lemnt.member.adapter.web.account.req;

@io.swagger.v3.oas.annotations.media.Schema(description = "WechatMobileUpdate请求")
public record WechatMobileUpdateReq(
        @io.swagger.v3.oas.annotations.media.Schema(description = "code")
                @jakarta.validation.constraints.NotBlank
                @jakarta.validation.constraints.Size(max = 256)
                String code)
        implements java.io.Serializable {
    @Override
    public String toString() {
        return "WechatMobileUpdateReq[redacted]";
    }
}

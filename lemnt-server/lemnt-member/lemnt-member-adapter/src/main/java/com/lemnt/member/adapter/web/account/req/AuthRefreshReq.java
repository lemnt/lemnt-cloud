package com.lemnt.member.adapter.web.account.req;

@io.swagger.v3.oas.annotations.media.Schema(description = "AuthRefresh请求")
public record AuthRefreshReq(
        @io.swagger.v3.oas.annotations.media.Schema(description = "会员刷新令牌，禁止记录到日志")
                @jakarta.validation.constraints.NotBlank
                @jakarta.validation.constraints.Size(max = 64)
                String refreshToken)
        implements java.io.Serializable {
    @Override
    public String toString() {
        return "AuthRefreshReq[redacted]";
    }
}

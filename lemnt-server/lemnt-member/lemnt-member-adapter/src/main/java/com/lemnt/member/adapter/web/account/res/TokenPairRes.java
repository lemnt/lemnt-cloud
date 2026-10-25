package com.lemnt.member.adapter.web.account.res;

@io.swagger.v3.oas.annotations.media.Schema(description = "会员TokenPair响应")
public record TokenPairRes(
        @io.swagger.v3.oas.annotations.media.Schema(description = "会员编号") Long memberId,
        @io.swagger.v3.oas.annotations.media.Schema(description = "会员访问令牌，禁止记录到日志")
                String accessToken,
        @io.swagger.v3.oas.annotations.media.Schema(description = "会员刷新令牌，禁止记录到日志")
                String refreshToken,
        @io.swagger.v3.oas.annotations.media.Schema(description = "访问令牌有效期，秒") Long expiresIn)
        implements java.io.Serializable {
    @Override
    public String toString() {
        return "TokenPairRes[tokens=<redacted>]";
    }
}

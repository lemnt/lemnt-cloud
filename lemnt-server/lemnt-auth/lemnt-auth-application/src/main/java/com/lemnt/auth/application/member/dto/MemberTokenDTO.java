package com.lemnt.auth.application.member.dto;

/**
 * 会员令牌数据传输对象。
 */
public record MemberTokenDTO(
        Long memberId,
        String accessToken,
        Long expiresIn
) implements java.io.Serializable {
    @Override
    public String toString() {
        return "MemberTokenDTO[tokens=<redacted>]";
    }
}

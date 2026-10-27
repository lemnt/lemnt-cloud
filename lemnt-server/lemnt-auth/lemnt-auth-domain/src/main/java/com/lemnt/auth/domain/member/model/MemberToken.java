package com.lemnt.auth.domain.member.model;

/**
 * 会员令牌。
 */
public record MemberToken(
        Long memberId,
        String accessToken,
        Long expiresIn
) {
    @Override
    public String toString() {
        return "MemberToken[memberId=" + memberId + ",tokens=<redacted>]";
    }
}

package com.lemnt.auth.domain.member.model;

/** 会员服务返回的登录资料，不承担数据库映射。 */
public record MemberLoginInfo(Long memberId, String mobile, int status, String passwordHash) {
    @Override
    public String toString() {
        return "MemberLoginInfo[memberId=" + memberId + ", status=" + status + ", passwordHash=[REDACTED]]";
    }
}

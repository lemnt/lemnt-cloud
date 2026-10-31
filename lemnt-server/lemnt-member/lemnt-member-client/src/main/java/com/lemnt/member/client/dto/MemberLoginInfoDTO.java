package com.lemnt.member.client.dto;

import java.io.Serializable;

/** 仅供认证中心使用的登录资料；密码哈希不进入日志或 HTTP 响应。 */
public record MemberLoginInfoDTO(Long memberId, String mobile, int status, String passwordHash)
        implements Serializable {
    @Override
    public String toString() {
        return "MemberLoginInfoDTO[memberId=" + memberId + ", status=" + status + ", passwordHash=[REDACTED]]";
    }
}

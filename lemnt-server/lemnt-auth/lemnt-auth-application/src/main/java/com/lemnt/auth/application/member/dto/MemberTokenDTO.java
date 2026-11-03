package com.lemnt.auth.application.member.dto;

import java.io.Serializable;

/**
 * 会员令牌数据传输对象。
 */
public record MemberTokenDTO(Long memberId, String accessToken, Long expiresIn)
        implements Serializable {
    @Override
    public String toString() {
        return "MemberTokenDTO[tokens=<redacted>]";
    }
}

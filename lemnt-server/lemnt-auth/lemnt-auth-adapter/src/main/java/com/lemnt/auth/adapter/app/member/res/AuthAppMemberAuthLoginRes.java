package com.lemnt.auth.adapter.app.member.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

/**
 * 会员令牌响应。
 */
@Schema(description = "会员MemberToken响应")
public record AuthAppMemberAuthLoginRes(
        @Schema(description = "会员编号")
        Long memberId,
        @Schema(description = "会员访问令牌，禁止记录到日志")
        String accessToken,
        @Schema(description = "访问令牌有效期，秒")
        Long expiresIn
) implements Serializable {
    @Override
    public String toString() {
        return "AuthAppMemberAuthLoginRes[tokens=<redacted>]";
    }
}

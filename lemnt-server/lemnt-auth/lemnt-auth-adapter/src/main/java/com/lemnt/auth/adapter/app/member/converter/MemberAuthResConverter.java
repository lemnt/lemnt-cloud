package com.lemnt.auth.adapter.app.member.converter;

import com.lemnt.auth.adapter.app.member.res.AuthAppMemberAuthLoginRes;
import com.lemnt.auth.adapter.app.member.res.AuthAppMemberAuthRefreshRes;
import com.lemnt.auth.application.member.dto.MemberTokenDTO;

/**
 * 小程序令牌输出契约转换。
 */
public final class MemberAuthResConverter {
    private MemberAuthResConverter() {}

    /**
     * 保持既有会员令牌响应字段。
     */
    public static AuthAppMemberAuthLoginRes toResToAuthAppMemberAuthLoginRes(MemberTokenDTO value) {
        return value == null ? null : new AuthAppMemberAuthLoginRes(
                value.memberId(), value.accessToken(), value.expiresIn());
    }

    /**
     * 保持既有会员令牌响应字段。
     */
    public static AuthAppMemberAuthRefreshRes toResToAuthAppMemberAuthRefreshRes(MemberTokenDTO value) {
        return value == null ? null : new AuthAppMemberAuthRefreshRes(
                value.memberId(), value.accessToken(), value.expiresIn());
    }
}

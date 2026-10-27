package com.lemnt.auth.adapter.app.member.converter;

import com.lemnt.auth.adapter.app.member.req.AuthLoginReq;
import com.lemnt.auth.application.member.cmd.AuthLoginCmd;

/**
 * 小程序 HTTP 请求转换为认证命令。
 */
public final class MemberAuthReqConverter {
    private MemberAuthReqConverter() {}

    /**
     * 转换密码登录请求。
     */
    public static AuthLoginCmd toLoginCmd(AuthLoginReq req) {
        return new AuthLoginCmd(req.mobile(), req.password());
    }

}

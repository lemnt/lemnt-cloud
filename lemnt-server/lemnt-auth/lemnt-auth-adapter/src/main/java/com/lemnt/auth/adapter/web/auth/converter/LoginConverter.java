package com.lemnt.auth.adapter.web.auth.converter;

import com.lemnt.auth.adapter.web.auth.req.LoginReq;
import com.lemnt.auth.adapter.web.auth.res.LoginRes;
import com.lemnt.auth.application.auth.cmd.LoginCmd;
import com.lemnt.auth.domain.user.entity.TokenResult;

/**
 * 授权对象组装器
 *
 * @author wangxx
 */
public class LoginConverter {

    public static LoginCmd toCmd(LoginReq item) {
        if (item == null) {
            return null;
        }
        return new LoginCmd(item.username(), item.password());
    }

    public static LoginRes toRes(TokenResult item) {
        if (item == null) {
            return null;
        }
        return new LoginRes(item.accessToken(), item.expiresIn());
    }
}

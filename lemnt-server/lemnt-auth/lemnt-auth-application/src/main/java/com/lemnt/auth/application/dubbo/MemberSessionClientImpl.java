package com.lemnt.auth.application.dubbo;

import com.lemnt.auth.api.MemberSessionClient;
import com.lemnt.auth.application.member.service.MemberAuthenticationService;

import org.apache.dubbo.config.annotation.DubboService;

import lombok.RequiredArgsConstructor;

/**
 * 网关和业务服务仅依赖此只读会话校验接口。
 */
@DubboService
@RequiredArgsConstructor
public class MemberSessionClientImpl implements MemberSessionClient {

    private final MemberAuthenticationService memberAuthenticationService;
    /**
     * 校验登录会话并返回会员编号。
     */
    public Long authenticate(String token) {
        return memberAuthenticationService.authenticate(token);
    }
}

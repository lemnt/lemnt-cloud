package com.lemnt.auth.application.dubbo;

import com.lemnt.auth.api.MemberSessionClient;
import com.lemnt.auth.application.member.service.MemberAuthenticationService;

import org.apache.dubbo.config.annotation.DubboService;

import lombok.RequiredArgsConstructor;

/**
 * 认证中心会话契约实现，网关不依赖Dubbo。
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
    /** 同步撤销会员会话，不回调会员数据库。 */
    @Override
    public void revokeMemberSessions(Long memberId) {
        memberAuthenticationService.revokeMemberSessions(memberId);
    }

}

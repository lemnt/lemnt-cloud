package com.lemnt.member.application.dubbo;

import com.lemnt.member.client.MemberSessionClient;
import com.lemnt.member.application.account.service.MemberAuthService;

@org.apache.dubbo.config.annotation.DubboService
@lombok.RequiredArgsConstructor
public class MemberSessionClientImpl implements MemberSessionClient {
    private final MemberAuthService service;

    public Long authenticate(String token) {
        return service.authenticate(token);
    }
}

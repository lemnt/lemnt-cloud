package com.lemnt.gateway.service;

import com.lemnt.member.client.MemberSessionClient;

@org.springframework.stereotype.Component
public class DubboMemberSessionLookup implements MemberSessionLookup {
    @org.apache.dubbo.config.annotation.DubboReference(check = false, timeout = 3000, retries = 0)
    private MemberSessionClient sessions;

    public Long authenticate(String token) {
        return sessions.authenticate(token);
    }
}

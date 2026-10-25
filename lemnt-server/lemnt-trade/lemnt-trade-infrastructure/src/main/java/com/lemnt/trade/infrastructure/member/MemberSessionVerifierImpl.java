package com.lemnt.trade.infrastructure.member;

import com.lemnt.member.client.MemberSessionClient;
import org.apache.dubbo.config.annotation.DubboReference;

@org.springframework.stereotype.Component
public class MemberSessionVerifierImpl implements MemberSessionVerifier {
    @DubboReference(check = false, retries = 0, timeout = 3000)
    private MemberSessionClient sessions;

    public Long authenticate(String token) {
        return sessions.authenticate(token);
    }
}

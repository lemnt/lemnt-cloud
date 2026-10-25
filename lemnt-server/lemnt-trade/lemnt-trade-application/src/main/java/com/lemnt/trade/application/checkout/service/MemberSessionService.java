package com.lemnt.trade.application.checkout.service;

import com.lemnt.trade.domain.gateway.MemberSessionGateway;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MemberSessionService {
    private final MemberSessionGateway sessions;
    private final CheckoutMemberService members;

    public Long requireMember(String authorization) {
        if (authorization == null
                || !authorization.startsWith("Bearer ")
                || authorization.length() > 4103) throw new SecurityException("会员登录已失效");
        String token = authorization.substring(7);
        if (token.isBlank() || token.chars().anyMatch(Character::isWhitespace))
            throw new SecurityException("会员登录已失效");
        Long memberId = sessions.authenticate(token);
        if (memberId == null || memberId <= 0) throw new SecurityException("会员登录已失效");
        members.requireActive(memberId);
        return memberId;
    }
}

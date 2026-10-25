package com.lemnt.trade.infrastructure.member;

import com.lemnt.trade.domain.gateway.MemberSessionGateway;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class MemberSessionGatewayImpl implements MemberSessionGateway {
    private final List<MemberSessionVerifier> verifiers;

    public MemberSessionGatewayImpl(List<MemberSessionVerifier> verifiers) {
        this.verifiers = List.copyOf(verifiers);
    }

    public Long authenticate(String token) {
        // No verifier is currently supplied: fail closed. Do not trust admin SecurityContext or
        // memberId headers.
        if (verifiers.size() != 1) return null;
        return verifiers.get(0).authenticate(token);
    }
}

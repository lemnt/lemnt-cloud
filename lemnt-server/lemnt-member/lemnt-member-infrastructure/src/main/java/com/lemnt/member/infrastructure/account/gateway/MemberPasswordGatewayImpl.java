package com.lemnt.member.infrastructure.account.gateway;

import com.lemnt.member.domain.account.gateway.MemberPasswordGateway;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@org.springframework.stereotype.Component
public class MemberPasswordGatewayImpl implements MemberPasswordGateway {
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
    private final String dummy = encoder.encode(java.util.UUID.randomUUID().toString());

    public String encode(String value) {
        return encoder.encode(value);
    }

    public boolean matches(String value, String hash) {
        return encoder.matches(value, hash == null ? dummy : hash);
    }
}

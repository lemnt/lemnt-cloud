package com.lemnt.member.domain.account.gateway;

public interface MemberPasswordGateway {
    String encode(String password);

    boolean matches(String password, String hash);
}

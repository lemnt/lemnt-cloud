package com.lemnt.trade.domain.gateway;

/** Validates an independent consumer session, never a backend admin identity/header. */
public interface MemberSessionGateway {
    Long authenticate(String token);
}

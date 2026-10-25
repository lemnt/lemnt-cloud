package com.lemnt.trade.infrastructure.member;

/**
 * Integration port: implement with authoritative member sessions, expiry/revocation and token
 * audience validation.
 */
public interface MemberSessionVerifier {
    Long authenticate(String token);
}

package com.lemnt.member.client;

/** Independent member-token validation; backend JWTs must never be accepted. */
public interface MemberSessionClient {
    Long authenticate(String accessToken);
}

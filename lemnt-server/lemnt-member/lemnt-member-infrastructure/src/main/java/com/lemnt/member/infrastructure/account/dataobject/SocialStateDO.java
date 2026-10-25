package com.lemnt.member.infrastructure.account.dataobject;

@lombok.Data
public class SocialStateDO {
    private String stateHash;
    private String provider;
    private String purpose;
    private Long memberId;
    private java.time.LocalDateTime expiresAt;
}

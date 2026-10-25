package com.lemnt.member.infrastructure.account.dataobject;

@lombok.Data
public class SessionDO {
    private String id;
    private Long memberId;
    private String accessHash;
    private String refreshHash;
    private java.time.LocalDateTime expiresAt;
    private java.time.LocalDateTime refreshExpiresAt;
}

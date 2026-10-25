package com.lemnt.member.infrastructure.account.dataobject;

@lombok.Data
public class SmsChallengeDO {
    private String mobile;
    private String scene;
    private String codeHash;
    private java.time.LocalDateTime expiresAt;
    private Integer attempts;
    private java.time.LocalDateTime lastSentAt;
    private java.time.LocalDate sentDate;
    private Integer dailyCount;
}

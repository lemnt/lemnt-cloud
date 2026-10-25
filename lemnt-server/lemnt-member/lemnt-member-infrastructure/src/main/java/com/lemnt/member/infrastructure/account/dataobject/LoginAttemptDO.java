package com.lemnt.member.infrastructure.account.dataobject;

@lombok.Data
public class LoginAttemptDO {
    private String loginHash;
    private Integer failures;
    private java.time.LocalDateTime windowStart;
}

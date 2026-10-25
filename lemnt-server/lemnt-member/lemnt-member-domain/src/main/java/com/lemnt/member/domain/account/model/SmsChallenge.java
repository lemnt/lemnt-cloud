package com.lemnt.member.domain.account.model;

public record SmsChallenge(
        String mobile,
        String scene,
        String codeHash,
        java.time.LocalDateTime expiresAt,
        Integer attempts,
        java.time.LocalDateTime lastSentAt,
        java.time.LocalDate sentDate,
        Integer dailyCount) {}

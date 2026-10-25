package com.lemnt.member.domain.account.model;

public record SocialState(
        String stateHash,
        String provider,
        String purpose,
        Long memberId,
        java.time.LocalDateTime expiresAt) {}

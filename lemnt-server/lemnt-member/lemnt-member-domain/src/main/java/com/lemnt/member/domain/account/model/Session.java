package com.lemnt.member.domain.account.model;

public record Session(
        String id,
        Long memberId,
        String accessHash,
        String refreshHash,
        java.time.LocalDateTime expiresAt,
        java.time.LocalDateTime refreshExpiresAt) {}

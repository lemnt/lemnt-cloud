package com.lemnt.member.domain.account.model;

public record LevelRecord(
        Long id,
        Long memberId,
        Long levelId,
        Long experience,
        String reason,
        java.time.LocalDateTime createTime) {}

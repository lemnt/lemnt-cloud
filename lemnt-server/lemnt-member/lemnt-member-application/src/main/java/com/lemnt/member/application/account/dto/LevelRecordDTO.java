package com.lemnt.member.application.account.dto;

public record LevelRecordDTO(
        Long id,
        Long memberId,
        Long levelId,
        Long experience,
        String reason,
        java.time.LocalDateTime createTime) {}

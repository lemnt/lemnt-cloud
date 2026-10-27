package com.lemnt.member.domain.growth.model;

import java.time.LocalDateTime;

/**
 * 等级记录。
 */
public record LevelRecord(
        Long id,
        Long memberId,
        Long levelId,
        Long experience,
        String reason,
        LocalDateTime createTime
) {}

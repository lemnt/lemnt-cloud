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
        LocalDateTime createTime,
        Integer level,
        Integer discountPercent,
        Long recordExperience,
        String description
) {
    /** 旧等级流水缺少配置快照，不回填当前配置。 */
    public LevelRecord(
            Long id,
            Long memberId,
            Long levelId,
            Long experience,
            String reason,
            LocalDateTime createTime) {
        this(id, memberId, levelId, experience, reason, createTime, null, null, null, null);
    }
}

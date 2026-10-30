package com.lemnt.member.application.growth.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 等级记录数据传输对象。
 */
public record LevelRecordDTO(
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
) implements Serializable {
    /** 旧等级流水缺少配置快照，不回填当前配置。 */
    public LevelRecordDTO(
            Long id,
            Long memberId,
            Long levelId,
            Long experience,
            String reason,
            LocalDateTime createTime) {
        this(id, memberId, levelId, experience, reason, createTime, null, null, null, null);
    }
}

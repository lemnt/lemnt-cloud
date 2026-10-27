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
        LocalDateTime createTime
) implements Serializable {}

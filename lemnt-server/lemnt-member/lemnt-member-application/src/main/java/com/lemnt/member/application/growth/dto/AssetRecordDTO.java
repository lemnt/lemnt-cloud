package com.lemnt.member.application.growth.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 资产记录数据传输对象。
 */
public record AssetRecordDTO(
        Long id,
        Long memberId,
        String assetType,
        Long delta,
        Long balance,
        String bizType,
        String bizNo,
        String reason,
        LocalDateTime createTime
) implements Serializable {}

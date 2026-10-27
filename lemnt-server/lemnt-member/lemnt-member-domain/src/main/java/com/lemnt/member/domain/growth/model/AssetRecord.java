package com.lemnt.member.domain.growth.model;

import java.time.LocalDateTime;

/**
 * 资产记录。
 */
public record AssetRecord(
        Long id,
        Long memberId,
        String assetType,
        Long delta,
        Long balance,
        String bizType,
        String bizNo,
        String reason,
        LocalDateTime createTime
) {}

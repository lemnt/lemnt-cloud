package com.lemnt.member.application.account.dto;

public record AssetRecordDTO(
        Long id,
        Long memberId,
        String assetType,
        Long delta,
        Long balance,
        String bizType,
        String bizNo,
        String reason,
        java.time.LocalDateTime createTime) {}

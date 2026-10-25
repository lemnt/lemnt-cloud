package com.lemnt.member.domain.account.model;

public record AssetRecord(
        Long id,
        Long memberId,
        String assetType,
        Long delta,
        Long balance,
        String bizType,
        String bizNo,
        String reason,
        java.time.LocalDateTime createTime) {}

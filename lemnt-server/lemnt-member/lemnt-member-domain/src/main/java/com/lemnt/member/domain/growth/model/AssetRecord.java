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
        LocalDateTime createTime,
        String nickname,
        Integer pointBizType,
        String title,
        String description
) {
    public AssetRecord(Long id,
        Long memberId,
        String assetType,
        Long delta,
        Long balance,
        String bizType,
        String bizNo,
        String reason,
        LocalDateTime createTime,
        String nickname) {this(id, memberId, assetType, delta, balance, bizType, bizNo, reason, createTime, nickname,null,null,null);}

    /** 不带昵称的既有资产投影兼容。 */
    public AssetRecord(Long id,
        Long memberId,
        String assetType,
        Long delta,
        Long balance,
        String bizType,
        String bizNo,
        String reason,
        LocalDateTime createTime) {this(id, memberId, assetType, delta, balance, bizType, bizNo, reason, createTime,null, null, null, null);}
}

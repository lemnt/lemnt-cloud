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
        LocalDateTime createTime,
        String nickname,
        Integer pointBizType,
        String title,
        String description,
        Integer experienceBizType
) implements Serializable {
    public AssetRecordDTO(Long id,
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
        String description) {this(id, memberId, assetType, delta, balance, bizType, bizNo, reason, createTime, nickname, pointBizType, title, description,null);}

    public AssetRecordDTO(Long id,
        Long memberId,
        String assetType,
        Long delta,
        Long balance,
        String bizType,
        String bizNo,
        String reason,
        LocalDateTime createTime,
        String nickname) {this(id, memberId, assetType, delta, balance, bizType, bizNo, reason, createTime, nickname,null,null,null,null);}

    /** 不带昵称的既有资产投影兼容。 */
    public AssetRecordDTO(Long id,
        Long memberId,
        String assetType,
        Long delta,
        Long balance,
        String bizType,
        String bizNo,
        String reason,
        LocalDateTime createTime) {this(id, memberId, assetType, delta, balance, bizType, bizNo, reason, createTime,null, null, null, null,null);}
}

package com.lemnt.member.application.growth.qry;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 查询会员资产记录分页查询。
 */
public record AssetRecordPageQry(
        Long memberId,
        String assetType,
        int pageNum,
        int pageSize,
        Boolean addStatus,
        LocalDateTime startTime,
        LocalDateTime endTime
) implements Serializable {
    /** 旧查询保持全部变更方向与时间。 */
    public AssetRecordPageQry(Long memberId, String assetType, int pageNum, int pageSize) {
        this(memberId, assetType, pageNum, pageSize, null, null, null);
    }
}

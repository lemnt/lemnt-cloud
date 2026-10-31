package com.lemnt.member.domain.growth.model;

import java.time.LocalDateTime;

/** 资产流水分页筛选条件。 */
public record AssetRecordPageFilter(
        Long memberId,
        String assetType,
        int offset,
        int limit,
        String nickname,
        String bizType,
        Integer pointBizType,
        String bizNo,
        Integer experienceBizType,
        String title,
        LocalDateTime startTime,
        LocalDateTime endTime,
        Boolean addStatus
) {
    /** 原后台/经验条件不附加积分收入支出。 */
    public AssetRecordPageFilter(Long memberId, String assetType, int offset, int limit,
            String nickname, String bizType, Integer pointBizType, String bizNo,
            Integer experienceBizType, String title, LocalDateTime startTime, LocalDateTime endTime) {
        this(memberId, assetType, offset, limit, nickname, bizType, pointBizType, bizNo,
                experienceBizType, title, startTime, endTime, null);
    }

    /** 旧分页调用不附加经验筛选。 */
    public AssetRecordPageFilter(Long memberId, String assetType, int offset, int limit,
            String nickname, String bizType, Integer pointBizType) {
        this(memberId, assetType, offset, limit, nickname, bizType, pointBizType,
                null, null, null, null, null);
    }

    public AssetRecordPageFilter(Long memberId,
        String assetType,
        int offset,
        int limit,
        String nickname,
        String bizType) {this(memberId, assetType, offset, limit, nickname, bizType,null);}

    public AssetRecordPageFilter(Long memberId,
        String assetType,
        int offset,
        int limit) {this(memberId, assetType, offset, limit,null,null,null);}
}

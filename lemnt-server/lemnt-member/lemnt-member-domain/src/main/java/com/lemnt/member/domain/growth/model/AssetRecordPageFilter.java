package com.lemnt.member.domain.growth.model;

/** 资产流水分页筛选条件。 */
public record AssetRecordPageFilter(
        Long memberId,
        String assetType,
        int offset,
        int limit,
        String nickname,
        String bizType,
        Integer pointBizType
) {
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

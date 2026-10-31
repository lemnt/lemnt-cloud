package com.lemnt.member.application.growth.qry;

import java.io.Serializable;

/** 资产流水分页查询。 */
public record AssetRecordAllPageQry(
        String assetType,
        Long memberId,
        int pageNum,
        int pageSize,
        String nickname,
        String bizType,
        Integer pointBizType,
        String title
) implements Serializable {
    /** 旧查询不附加标题条件。 */
    public AssetRecordAllPageQry(String assetType, Long memberId, int pageNum, int pageSize,
            String nickname, String bizType, Integer pointBizType) {
        this(assetType, memberId, pageNum, pageSize, nickname, bizType, pointBizType, null);
    }

    public AssetRecordAllPageQry(String assetType,
        Long memberId,
        int pageNum,
        int pageSize,
        String nickname,
        String bizType) {this(assetType, memberId, pageNum, pageSize, nickname, bizType,null);}

    public AssetRecordAllPageQry(String assetType,
        Long memberId,
        int pageNum,
        int pageSize) {this(assetType, memberId, pageNum, pageSize,null,null,null);}
}

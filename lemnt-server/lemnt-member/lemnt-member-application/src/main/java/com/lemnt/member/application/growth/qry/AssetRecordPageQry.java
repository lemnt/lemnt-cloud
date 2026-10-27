package com.lemnt.member.application.growth.qry;

import java.io.Serializable;

/**
 * 查询会员资产记录分页查询。
 */
public record AssetRecordPageQry(
        Long memberId,
        String assetType,
        int pageNum,
        int pageSize
) implements Serializable {}

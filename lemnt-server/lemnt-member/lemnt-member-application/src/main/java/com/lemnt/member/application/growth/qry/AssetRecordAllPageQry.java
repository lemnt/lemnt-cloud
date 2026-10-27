package com.lemnt.member.application.growth.qry;

import java.io.Serializable;

/** 资产流水分页查询。 */
public record AssetRecordAllPageQry(
        String assetType,
        Long memberId,
        int pageNum,
        int pageSize
) implements Serializable {}

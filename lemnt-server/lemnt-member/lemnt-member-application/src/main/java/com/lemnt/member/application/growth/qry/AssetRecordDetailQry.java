package com.lemnt.member.application.growth.qry;

import java.io.Serializable;

/** 资产流水详情查询。 */
public record AssetRecordDetailQry(
        String assetType,
        Long id
) implements Serializable {}

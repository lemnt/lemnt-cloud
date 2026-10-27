package com.lemnt.member.domain.growth.model;

/** 资产流水分页筛选条件。 */
public record AssetRecordPageFilter(
        Long memberId,
        String assetType,
        int offset,
        int limit
) {}

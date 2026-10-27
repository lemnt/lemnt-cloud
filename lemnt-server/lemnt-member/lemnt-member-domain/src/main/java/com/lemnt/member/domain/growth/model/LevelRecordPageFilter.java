package com.lemnt.member.domain.growth.model;

/** 等级变更流水分页筛选条件。 */
public record LevelRecordPageFilter(
        Long memberId,
        int offset,
        int limit
) {}

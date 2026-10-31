package com.lemnt.member.domain.growth.model;

import java.time.LocalDateTime;

/** 等级变更流水分页筛选条件。 */
public record LevelRecordPageFilter(
        Long memberId,
        int offset,
        int limit,
        Long levelId,
        LocalDateTime startTime,
        LocalDateTime endTime
) {
    /** 会员下列表沿用无附加条件的分页。 */
    public LevelRecordPageFilter(Long memberId, int offset, int limit) {
        this(memberId, offset, limit, null, null, null);
    }
}

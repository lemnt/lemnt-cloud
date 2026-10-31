package com.lemnt.member.application.growth.qry;

import java.io.Serializable;
import java.time.LocalDateTime;

/** 等级变更流水分页查询。 */
public record LevelRecordAllPageQry(
        Long memberId,
        int pageNum,
        int pageSize,
        Long levelId,
        LocalDateTime startTime,
        LocalDateTime endTime
) implements Serializable {
    /** 会员内旧查询不附加等级或日期筛选。 */
    public LevelRecordAllPageQry(Long memberId, int pageNum, int pageSize) {
        this(memberId, pageNum, pageSize, null, null, null);
    }
}

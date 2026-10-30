package com.lemnt.member.domain.growth.model;

import java.time.LocalDateTime;

/** 签到记录分页筛选条件。 */
public record SignRecordPageFilter(
        Long memberId,
        int offset,
        int limit,
        String nickname,
        Integer day,
        LocalDateTime startTime,
        LocalDateTime endTime
) {
    /** 旧查询不附加天数或时间。 */
    public SignRecordPageFilter(Long memberId, int offset, int limit, String nickname) {
        this(memberId, offset, limit, nickname, null, null, null);
    }
    public SignRecordPageFilter(Long memberId,int offset,int limit) {this(memberId,offset,limit,null);}
}

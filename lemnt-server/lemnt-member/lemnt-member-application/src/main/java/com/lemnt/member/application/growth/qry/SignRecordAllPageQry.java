package com.lemnt.member.application.growth.qry;

import java.io.Serializable;
import java.time.LocalDateTime;

/** 签到记录分页查询。 */
public record SignRecordAllPageQry(
        Long memberId,
        int pageNum,
        int pageSize,
        String nickname,
        Integer day,
        LocalDateTime startTime,
        LocalDateTime endTime
) implements Serializable {
    /** 旧查询不附加天数或时间。 */
    public SignRecordAllPageQry(Long memberId, int pageNum, int pageSize, String nickname) {
        this(memberId, pageNum, pageSize, nickname, null, null, null);
    }
    public SignRecordAllPageQry(Long memberId,int pageNum,int pageSize) {this(memberId,pageNum,pageSize,null);}
}

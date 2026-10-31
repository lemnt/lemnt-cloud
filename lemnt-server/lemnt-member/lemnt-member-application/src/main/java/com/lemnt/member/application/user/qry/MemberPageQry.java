package com.lemnt.member.application.user.qry;

import java.time.LocalDateTime;
import java.util.List;

import java.io.Serializable;

/** 会员资料分页查询，不复用流水查询参数。 */
public record MemberPageQry(
        String mobile,
        String email,
        String nickname,
        Long levelId,
        Long groupId,
        List<Long> tagIds,
        LocalDateTime loginStartTime,
        LocalDateTime loginEndTime,
        LocalDateTime createStartTime,
        LocalDateTime createEndTime,
        int pageNum,
        int pageSize
) implements Serializable {
    /** 原调用不附加新筛选。 */
    public MemberPageQry(String mobile, int pageNum, int pageSize) {
        this(mobile, null, null, null, null, null, null, null, null, null, pageNum, pageSize);
    }
}

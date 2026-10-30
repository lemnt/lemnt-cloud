package com.lemnt.member.domain.growth.model;

/** 签到记录分页筛选条件。 */
public record SignRecordPageFilter(
        Long memberId,
        int offset,
        int limit,
        String nickname
) {
    public SignRecordPageFilter(Long memberId,int offset,int limit) {this(memberId,offset,limit,null);}
}

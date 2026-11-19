package com.lemnt.member.domain.user.model;

import java.time.LocalDate;

/** 会员库按天和档案字段聚合后的计数。 */
public final class MemberStatistics {
    private MemberStatistics() {}

    /** 会员创建日期和当天新增人数。 */
    public record Registration(LocalDate date, long count) {}

    /** 档案字段编号和人数，编号为空表示未记录。 */
    public record Distribution(Integer value, long count) {}
}

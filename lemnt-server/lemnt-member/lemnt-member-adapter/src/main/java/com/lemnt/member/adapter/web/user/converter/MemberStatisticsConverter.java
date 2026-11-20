package com.lemnt.member.adapter.web.user.converter;

import com.lemnt.member.adapter.web.user.req.MemberStatisticsReq;
import com.lemnt.member.adapter.web.user.res.MemberStatisticsRes;
import com.lemnt.member.application.user.dto.MemberStatisticsDTO;
import com.lemnt.member.application.user.qry.MemberStatisticsQry;
import com.lemnt.member.domain.user.model.MemberStatistics.Distribution;

/** 转换会员统计请求与响应。 */
public final class MemberStatisticsConverter {
    private MemberStatisticsConverter() {}

    /** 保留请求起止日期，不做时区转换。 */
    public static MemberStatisticsQry toQry(MemberStatisticsReq req) {
        return new MemberStatisticsQry(req.from(), req.to());
    }

    /** 将领域计数转换为公开响应。 */
    public static MemberStatisticsRes toRes(MemberStatisticsDTO result) {
        return new MemberStatisticsRes(
                result.registrations().stream()
                        .map(
                                row ->
                                        new MemberStatisticsRes.Registration(
                                                row.date().toString(), row.count()))
                        .toList(),
                result.sexes().stream().map(MemberStatisticsConverter::distribution).toList(),
                result.areas().stream().map(MemberStatisticsConverter::distribution).toList());
    }

    private static MemberStatisticsRes.Distribution distribution(Distribution row) {
        return new MemberStatisticsRes.Distribution(row.value(), row.count());
    }
}

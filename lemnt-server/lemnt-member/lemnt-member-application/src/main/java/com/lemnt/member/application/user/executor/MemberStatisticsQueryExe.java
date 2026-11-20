package com.lemnt.member.application.user.executor;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.member.application.user.dto.MemberStatisticsDTO;
import com.lemnt.member.application.user.qry.MemberStatisticsQry;
import com.lemnt.member.domain.user.gateway.MemberUserGateway;
import com.lemnt.member.domain.user.model.MemberStatistics.Registration;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;

/** 统计会员注册和档案分布。 */
@Component
@RequiredArgsConstructor
public class MemberStatisticsQueryExe {
    private final MemberUserGateway users;

    /** 查询最多366个自然日，无新增记录的日期计为零。 */
    @Transactional(readOnly = true)
    public MemberStatisticsDTO execute(MemberStatisticsQry qry) {
        var from = qry.from();
        var to = qry.to();
        if (to.isBefore(from) || ChronoUnit.DAYS.between(from, to) >= 366) {
            throw new ServiceException("请选择不超过366天的注册日期范围", 400);
        }
        var until = to.plusDays(1);
        var counts =
                users.countRegistrations(from.atStartOfDay(), until.atStartOfDay()).stream()
                        .collect(Collectors.toMap(Registration::date, Registration::count));
        var trend =
                from.datesUntil(until)
                        .map(date -> new Registration(date, counts.getOrDefault(date, 0L)))
                        .toList();
        return new MemberStatisticsDTO(trend, users.countBySex(), users.countByArea());
    }
}

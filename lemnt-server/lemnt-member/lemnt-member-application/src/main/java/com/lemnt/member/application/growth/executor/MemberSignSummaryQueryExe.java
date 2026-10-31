package com.lemnt.member.application.growth.executor;

import com.lemnt.member.application.growth.dto.SignSummaryDTO;
import com.lemnt.member.application.growth.qry.SignSummaryQry;
import com.lemnt.member.domain.growth.gateway.MemberSignRecordGateway;
import com.lemnt.member.domain.shared.model.MemberRules;
import com.lemnt.member.domain.user.gateway.MemberUserGateway;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 签到汇总查询执行器。 */
@Component
@RequiredArgsConstructor
public class MemberSignSummaryQueryExe {

    private final MemberSignRecordGateway memberSignRecordGateway;
    private final MemberUserGateway memberUserGateway;

    /**
     * 执行签到汇总查询。
     */
    public SignSummaryDTO execute(SignSummaryQry qry) {
        MemberRules.id(qry.memberId());
        var today = memberUserGateway.now().toLocalDate();
        var last = memberSignRecordGateway.lastSign(qry.memberId());
        int continuous =
                last != null && (last.signDate().equals(today) || last.signDate().equals(today.minusDays(1)))
                        ? last.day() : 0;
        return new SignSummaryDTO(
                memberSignRecordGateway.signCount(qry.memberId()),
                continuous,
                last != null && last.signDate().equals(today));
    }
}

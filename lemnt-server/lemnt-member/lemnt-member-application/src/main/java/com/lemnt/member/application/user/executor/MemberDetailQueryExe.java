package com.lemnt.member.application.user.executor;

import com.lemnt.member.application.user.converter.MemberUserDTOConverter;
import com.lemnt.member.application.user.dto.MemberDTO;
import com.lemnt.member.application.user.qry.MemberDetailQry;
import com.lemnt.member.domain.shared.model.MemberRules;
import com.lemnt.member.domain.user.gateway.MemberUserGateway;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 资料查询执行器。 */
@Component
@RequiredArgsConstructor
public class MemberDetailQueryExe {

    private final MemberUserGateway memberUserGateway;

    /**
     * 执行资料查询。
     */
    public MemberDTO execute(MemberDetailQry qry) {
        MemberRules.id(qry.memberId());
        return MemberUserDTOConverter.toMemberDTO(memberUserGateway.findById(qry.memberId()));
    }
}

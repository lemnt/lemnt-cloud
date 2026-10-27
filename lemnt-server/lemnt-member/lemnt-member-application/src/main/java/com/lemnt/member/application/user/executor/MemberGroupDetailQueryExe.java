package com.lemnt.member.application.user.executor;

import com.lemnt.member.application.user.converter.MemberUserDTOConverter;
import com.lemnt.member.application.user.dto.GroupDTO;
import com.lemnt.member.application.user.qry.GroupDetailQry;
import com.lemnt.member.domain.shared.model.MemberRules;
import com.lemnt.member.domain.user.gateway.MemberGroupGateway;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 分组详情查询执行器。 */
@Component
@RequiredArgsConstructor
public class MemberGroupDetailQueryExe {

    private final MemberGroupGateway memberGroupGateway;

    /**
     * 执行分组详情查询。
     */
    public GroupDTO execute(GroupDetailQry qry) {
        MemberRules.id(qry.id());
        return MemberUserDTOConverter.toGroupDTO(memberGroupGateway.group(qry.id()));
    }
}

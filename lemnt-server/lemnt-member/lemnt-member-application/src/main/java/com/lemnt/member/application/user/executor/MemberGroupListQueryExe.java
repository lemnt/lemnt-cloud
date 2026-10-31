package com.lemnt.member.application.user.executor;

import com.lemnt.member.application.user.converter.MemberUserDTOConverter;
import com.lemnt.member.application.user.dto.GroupDTO;
import com.lemnt.member.application.user.qry.GroupListQry;
import com.lemnt.member.domain.user.gateway.MemberGroupGateway;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

/** 查询分组查询执行器。 */
@Component
@RequiredArgsConstructor
public class MemberGroupListQueryExe {

    private final MemberGroupGateway memberGroupGateway;

    /**
     * 执行查询分组查询。
     */
    public List<GroupDTO> execute(GroupListQry qry) {
        return memberGroupGateway.groups().stream().map(MemberUserDTOConverter::toGroupDTO).toList();
    }
}

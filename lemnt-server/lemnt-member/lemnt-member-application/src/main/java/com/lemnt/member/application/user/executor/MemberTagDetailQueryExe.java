package com.lemnt.member.application.user.executor;

import com.lemnt.member.application.user.converter.MemberUserDTOConverter;
import com.lemnt.member.application.user.dto.TagDTO;
import com.lemnt.member.application.user.qry.TagDetailQry;
import com.lemnt.member.domain.shared.model.MemberRules;
import com.lemnt.member.domain.user.gateway.MemberTagGateway;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 标签详情查询执行器。 */
@Component
@RequiredArgsConstructor
public class MemberTagDetailQueryExe {

    private final MemberTagGateway memberTagGateway;

    /**
     * 执行标签详情查询。
     */
    public TagDTO execute(TagDetailQry qry) {
        MemberRules.id(qry.id());
        return MemberUserDTOConverter.toTagDTO(memberTagGateway.tag(qry.id()));
    }
}

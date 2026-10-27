package com.lemnt.member.application.user.executor;

import com.lemnt.member.application.user.qry.MemberTagIdListQry;
import com.lemnt.member.domain.shared.model.MemberRules;
import com.lemnt.member.domain.user.gateway.MemberTagLinkGateway;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

/** 查询标签查询执行器。 */
@Component
@RequiredArgsConstructor
public class MemberTagIdListQueryExe {

    private final MemberTagLinkGateway memberTagLinkGateway;

    /**
     * 执行查询标签查询。
     */
    public List<Long> execute(MemberTagIdListQry qry) {
        MemberRules.id(qry.memberId());
        return memberTagLinkGateway.memberTags(qry.memberId());
    }
}

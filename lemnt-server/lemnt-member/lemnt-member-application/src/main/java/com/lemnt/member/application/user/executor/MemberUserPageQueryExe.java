package com.lemnt.member.application.user.executor;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.member.application.user.dto.MemberDTO;
import com.lemnt.member.application.user.qry.MemberPageQry;
import com.lemnt.member.application.user.service.MemberUserRelationQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** 后台会员分页专用返显，不改基础查询的App契约。 */
@Component
@RequiredArgsConstructor
public class MemberUserPageQueryExe {
    private final MemberPageQueryExe query;
    private final MemberUserRelationQueryService relations;
    /** 批量补关联信息并保留真实分页总数。 */
    public PageResult<MemberDTO> execute(MemberPageQry qry) {
        var page = query.execute(qry);
        return new PageResult<>(relations.enrich(page.getRecords()), page.getTotal());
    }
}

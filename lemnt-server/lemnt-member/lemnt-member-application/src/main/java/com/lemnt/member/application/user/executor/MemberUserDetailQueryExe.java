package com.lemnt.member.application.user.executor;

import com.lemnt.member.application.user.dto.MemberDTO;
import com.lemnt.member.application.user.qry.MemberDetailQry;
import com.lemnt.member.application.user.service.MemberUserRelationQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;

/** 后台会员详情补当前关联，未知会员保持NULL。 */
@Component
@RequiredArgsConstructor
public class MemberUserDetailQueryExe {
    private final MemberDetailQueryExe query;
    private final MemberUserRelationQueryService relations;
    /** 详情与分页共享批量关联规则。 */
    public MemberDTO execute(MemberDetailQry qry) {
        var value = query.execute(qry);
        return value == null ? null : relations.enrich(List.of(value)).get(0);
    }
}

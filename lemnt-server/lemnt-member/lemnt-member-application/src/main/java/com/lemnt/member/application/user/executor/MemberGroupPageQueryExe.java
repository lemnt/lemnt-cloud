package com.lemnt.member.application.user.executor;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.common.domain.model.PageResult;
import com.lemnt.member.application.user.converter.MemberUserDTOConverter;
import com.lemnt.member.application.user.dto.GroupDTO;
import com.lemnt.member.application.user.qry.GroupPageQry;
import com.lemnt.member.domain.shared.model.MemberRules;
import com.lemnt.member.domain.user.gateway.MemberGroupGateway;
import com.lemnt.member.domain.user.model.GroupPageFilter;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 会员分组分页执行器。 */
@Component
@RequiredArgsConstructor
public class MemberGroupPageQueryExe {

    private final MemberGroupGateway memberGroupGateway;

    /** 查询分组分页，沿用资源归属和分页约束。 */
    public PageResult<GroupDTO> execute(GroupPageQry qry) {
        MemberRules.page(qry.pageNum(), qry.pageSize());
        if (qry.status() != null && qry.status() != 0 && qry.status() != 1) {
            throw new ServiceException("状态不合法", 400);
        }
        var filter = new GroupPageFilter(MemberRules.optional(qry.name(), 100), qry.status(),
                (qry.pageNum() - 1) * qry.pageSize(), qry.pageSize());
        return new PageResult<>(
                memberGroupGateway.groups(filter).stream().map(MemberUserDTOConverter::toGroupDTO).toList(),
                memberGroupGateway.groupsCount(filter));
    }
}

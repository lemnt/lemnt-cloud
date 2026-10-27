package com.lemnt.member.application.user.executor;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.member.application.user.converter.MemberUserDTOConverter;
import com.lemnt.member.application.user.dto.MemberDTO;
import com.lemnt.member.application.user.qry.MemberPageQry;
import com.lemnt.member.domain.shared.model.MemberRules;
import com.lemnt.member.domain.user.gateway.MemberUserGateway;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 查询会员资料分页查询执行器。 */
@Component
@RequiredArgsConstructor
public class MemberPageQueryExe {

    private final MemberUserGateway memberUserGateway;

    /**
     * 执行查询会员资料分页查询。
     */
    public PageResult<MemberDTO> execute(MemberPageQry qry) {
        MemberRules.page(qry.pageNum(), qry.pageSize());
        String mobile = MemberRules.optional(qry.mobile(), 24);
        return new PageResult<>(
                memberUserGateway.findPage(mobile, (qry.pageNum() - 1) * qry.pageSize(), qry.pageSize()).stream()
                .map(MemberUserDTOConverter::toMemberDTO)
                .toList(),
                memberUserGateway.count(mobile));
    }
}

package com.lemnt.member.application.user.executor;

import com.lemnt.member.domain.user.model.MemberPageFilter;

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
        if (qry.levelId() != null) MemberRules.id(qry.levelId());
        if (qry.groupId() != null) MemberRules.id(qry.groupId());
        var tags = qry.tagIds() == null ? java.util.List.<Long>of() : qry.tagIds();
        tags.forEach(MemberRules::id);
        if ((qry.loginStartTime() != null && qry.loginEndTime() != null && qry.loginStartTime().isAfter(qry.loginEndTime()))
                || (qry.createStartTime() != null && qry.createEndTime() != null && qry.createStartTime().isAfter(qry.createEndTime()))) {
            throw new com.lemnt.common.core.exception.ServiceException("开始时间不能晚于结束时间", 400);
        }
        var filter = new MemberPageFilter(
                MemberRules.optional(qry.mobile(), 24), MemberRules.optional(qry.email(), 254),
                MemberRules.optional(qry.nickname(), 80), qry.levelId(), qry.groupId(), tags.stream().distinct().toList(),
                qry.loginStartTime(), qry.loginEndTime(), qry.createStartTime(), qry.createEndTime(),
                (qry.pageNum() - 1) * qry.pageSize(), qry.pageSize());
        return new PageResult<>(memberUserGateway.findPage(filter).stream()
                .map(MemberUserDTOConverter::toMemberDTO).toList(), memberUserGateway.count(filter));
    }
}

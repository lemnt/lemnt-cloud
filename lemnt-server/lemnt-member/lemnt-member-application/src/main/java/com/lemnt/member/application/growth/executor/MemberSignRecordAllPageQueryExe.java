package com.lemnt.member.application.growth.executor;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.member.application.growth.converter.MemberGrowthDTOConverter;
import com.lemnt.member.application.growth.dto.SignRecordDTO;
import com.lemnt.member.application.growth.qry.SignRecordAllPageQry;
import com.lemnt.member.domain.growth.gateway.MemberSignRecordGateway;
import com.lemnt.member.domain.growth.model.SignRecordPageFilter;
import com.lemnt.member.domain.shared.model.MemberRules;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 会员签到记录分页执行器。 */
@Component
@RequiredArgsConstructor
public class MemberSignRecordAllPageQueryExe {

    private final MemberSignRecordGateway memberSignRecordGateway;

    /** 查询签到记录分页，沿用资源归属和分页约束。 */
    public PageResult<SignRecordDTO> execute(SignRecordAllPageQry qry) {
        MemberRules.page(qry.pageNum(), qry.pageSize());
        if (qry.memberId() != null) {
            MemberRules.id(qry.memberId());
        }
        if (qry.nickname() != null && qry.nickname().length()>80) throw new com.lemnt.common.core.exception.ServiceException("昵称不能超过80字",400);
        var nickname=qry.nickname() == null || qry.nickname().isBlank() ? null : qry.nickname().trim();
        var filter = new SignRecordPageFilter(qry.memberId(),
                (qry.pageNum() - 1) * qry.pageSize(), qry.pageSize(),nickname);
        return new PageResult<>(
                memberSignRecordGateway.signs(filter).stream().map(MemberGrowthDTOConverter::toSignRecordDTO).toList(),
                memberSignRecordGateway.signsCount(filter));
    }
}

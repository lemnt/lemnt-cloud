package com.lemnt.member.application.growth.executor;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.member.application.growth.converter.MemberGrowthDTOConverter;
import com.lemnt.member.application.growth.dto.SignRecordDTO;
import com.lemnt.member.application.growth.qry.SignRecordPageQry;
import com.lemnt.member.domain.growth.gateway.MemberSignRecordGateway;
import com.lemnt.member.domain.shared.model.MemberRules;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 查询签到记录分页查询执行器。 */
@Component
@RequiredArgsConstructor
public class MemberSignRecordPageQueryExe {

    private final MemberSignRecordGateway memberSignRecordGateway;

    /**
     * 执行查询签到记录分页查询。
     */
    public PageResult<SignRecordDTO> execute(SignRecordPageQry qry) {
        MemberRules.id(qry.memberId());
        MemberRules.page(qry.pageNum(), qry.pageSize());
        return new PageResult<>(
                memberSignRecordGateway.signs(qry.memberId(), (qry.pageNum() - 1) * qry.pageSize(), qry.pageSize())
                .stream()
                .map(MemberGrowthDTOConverter::toSignRecordDTO)
                .toList(),
                memberSignRecordGateway.signCount(qry.memberId()));
    }
}

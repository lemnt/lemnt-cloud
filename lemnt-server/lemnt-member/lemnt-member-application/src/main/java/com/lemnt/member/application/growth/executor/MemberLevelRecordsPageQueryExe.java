package com.lemnt.member.application.growth.executor;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.member.application.growth.converter.MemberGrowthDTOConverter;
import com.lemnt.member.application.growth.dto.LevelRecordDTO;
import com.lemnt.member.application.growth.qry.LevelRecordsPageQry;
import com.lemnt.member.domain.growth.gateway.MemberLevelRecordGateway;
import com.lemnt.member.domain.shared.model.MemberRules;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 等级记录分页查询执行器。 */
@Component
@RequiredArgsConstructor
public class MemberLevelRecordsPageQueryExe {

    private final MemberLevelRecordGateway memberLevelRecordGateway;

    /**
     * 执行等级记录分页查询。
     */
    public PageResult<LevelRecordDTO> execute(LevelRecordsPageQry qry) {
        MemberRules.id(qry.memberId());
        MemberRules.page(qry.pageNum(), qry.pageSize());
        return new PageResult<>(
                memberLevelRecordGateway.levelRecords(qry.memberId(), (qry.pageNum() - 1) * qry.pageSize(), qry.pageSize())
                .stream()
                .map(MemberGrowthDTOConverter::toLevelRecordDTO)
                .toList(),
                memberLevelRecordGateway.levelRecordCount(qry.memberId()));
    }
}

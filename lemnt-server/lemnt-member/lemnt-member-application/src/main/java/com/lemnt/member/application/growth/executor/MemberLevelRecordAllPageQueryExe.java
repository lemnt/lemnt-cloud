package com.lemnt.member.application.growth.executor;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.member.application.growth.converter.MemberGrowthDTOConverter;
import com.lemnt.member.application.growth.dto.LevelRecordDTO;
import com.lemnt.member.application.growth.qry.LevelRecordAllPageQry;
import com.lemnt.member.domain.growth.gateway.MemberLevelRecordGateway;
import com.lemnt.member.domain.growth.model.LevelRecordPageFilter;
import com.lemnt.member.domain.shared.model.MemberRules;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 会员等级变更流水分页执行器。 */
@Component
@RequiredArgsConstructor
public class MemberLevelRecordAllPageQueryExe {

    private final MemberLevelRecordGateway memberLevelRecordGateway;

    /** 查询等级变更流水分页，沿用资源归属和分页约束。 */
    public PageResult<LevelRecordDTO> execute(LevelRecordAllPageQry qry) {
        MemberRules.page(qry.pageNum(), qry.pageSize());
        if (qry.memberId() != null) {
            MemberRules.id(qry.memberId());
        }
        var filter = new LevelRecordPageFilter(qry.memberId(),
                (qry.pageNum() - 1) * qry.pageSize(), qry.pageSize());
        return new PageResult<>(
                memberLevelRecordGateway.levels(filter).stream().map(MemberGrowthDTOConverter::toLevelRecordDTO).toList(),
                memberLevelRecordGateway.levelsCount(filter));
    }
}

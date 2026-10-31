package com.lemnt.member.application.growth.executor;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.common.core.exception.ServiceException;
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
        if (qry.levelId() != null) {
            MemberRules.id(qry.levelId());
        }
        if (qry.startTime() != null && qry.endTime() != null && qry.startTime().isAfter(qry.endTime())) {
            throw new ServiceException("开始时间不能晚于结束时间", 400);
        }
        var filter = new LevelRecordPageFilter(qry.memberId(),
                (qry.pageNum() - 1) * qry.pageSize(), qry.pageSize(),
                qry.levelId(), qry.startTime(), qry.endTime());
        return new PageResult<>(
                memberLevelRecordGateway.levels(filter).stream().map(MemberGrowthDTOConverter::toLevelRecordDTO).toList(),
                memberLevelRecordGateway.levelsCount(filter));
    }
}

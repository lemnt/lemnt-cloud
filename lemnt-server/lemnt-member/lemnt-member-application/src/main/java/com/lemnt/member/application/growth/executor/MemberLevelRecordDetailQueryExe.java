package com.lemnt.member.application.growth.executor;

import com.lemnt.member.application.growth.converter.MemberGrowthDTOConverter;
import com.lemnt.member.application.growth.dto.LevelRecordDTO;
import com.lemnt.member.application.growth.qry.LevelRecordDetailQry;
import com.lemnt.member.domain.growth.gateway.MemberLevelRecordGateway;
import com.lemnt.member.domain.shared.model.MemberRules;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 会员等级变更流水详情执行器。 */
@Component
@RequiredArgsConstructor
public class MemberLevelRecordDetailQueryExe {

    private final MemberLevelRecordGateway memberLevelRecordGateway;

    /** 查询等级变更流水详情，沿用资源归属和分页约束。 */
    public LevelRecordDTO execute(LevelRecordDetailQry qry) {
        MemberRules.id(qry.id());
        var record = memberLevelRecordGateway.levelsDetail(qry.id());
        return MemberGrowthDTOConverter.toLevelRecordDTO(record);
    }
}

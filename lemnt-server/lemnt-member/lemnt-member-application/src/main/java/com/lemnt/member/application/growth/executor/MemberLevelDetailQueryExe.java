package com.lemnt.member.application.growth.executor;

import com.lemnt.member.application.growth.converter.MemberGrowthDTOConverter;
import com.lemnt.member.application.growth.dto.LevelDTO;
import com.lemnt.member.application.growth.qry.LevelDetailQry;
import com.lemnt.member.domain.growth.gateway.MemberLevelGateway;
import com.lemnt.member.domain.shared.model.MemberRules;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 等级详情查询执行器。 */
@Component
@RequiredArgsConstructor
public class MemberLevelDetailQueryExe {

    private final MemberLevelGateway memberLevelGateway;

    /**
     * 执行等级详情查询。
     */
    public LevelDTO execute(LevelDetailQry qry) {
        MemberRules.id(qry.id());
        return MemberGrowthDTOConverter.toLevelDTO(memberLevelGateway.level(qry.id()));
    }
}

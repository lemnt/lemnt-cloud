package com.lemnt.member.application.growth.executor;

import com.lemnt.member.application.growth.converter.MemberGrowthDTOConverter;
import com.lemnt.member.application.growth.dto.LevelDTO;
import com.lemnt.member.application.growth.qry.LevelListQry;
import com.lemnt.member.domain.growth.gateway.MemberLevelGateway;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

/** 查询等级查询执行器。 */
@Component
@RequiredArgsConstructor
public class MemberLevelListQueryExe {

    private final MemberLevelGateway memberLevelGateway;

    /**
     * 执行查询等级查询。
     */
    public List<LevelDTO> execute(LevelListQry qry) {
        return memberLevelGateway.levels().stream()
                .filter(v -> !qry.enabledOnly() || v.status() == 1)
                .map(MemberGrowthDTOConverter::toLevelDTO)
                .toList();
    }
}

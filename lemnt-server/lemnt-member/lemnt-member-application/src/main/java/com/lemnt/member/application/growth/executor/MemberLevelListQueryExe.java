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
        if (qry.name() != null && qry.name().length() > 80) {
            throw new com.lemnt.common.core.exception.ServiceException("等级名称不能超过80字", 400);
        }
        if (qry.status() != null && qry.status() != 0 && qry.status() != 1) {
            throw new com.lemnt.common.core.exception.ServiceException("等级状态不合法", 400);
        }
        var name = qry.name() == null || qry.name().isBlank() ? null : qry.name().trim();
        return memberLevelGateway.levels(name, qry.enabledOnly() ? 1 : qry.status()).stream()
                .map(MemberGrowthDTOConverter::toLevelDTO).toList();
    }
}

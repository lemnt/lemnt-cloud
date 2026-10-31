package com.lemnt.member.application.growth.executor;

import com.lemnt.member.application.growth.converter.MemberGrowthDTOConverter;
import com.lemnt.member.application.growth.dto.ConfigDTO;
import com.lemnt.member.application.growth.qry.ConfigDetailQry;
import com.lemnt.member.domain.growth.gateway.MemberConfigGateway;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 配置查询执行器。 */
@Component
@RequiredArgsConstructor
public class MemberConfigDetailQueryExe {

    private final MemberConfigGateway memberConfigGateway;

    /**
     * 执行配置查询。
     */
    public ConfigDTO execute(ConfigDetailQry qry) {
        return MemberGrowthDTOConverter.toConfigDTO(memberConfigGateway.config());
    }
}

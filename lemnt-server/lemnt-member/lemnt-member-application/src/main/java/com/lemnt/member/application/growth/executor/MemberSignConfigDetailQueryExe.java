package com.lemnt.member.application.growth.executor;

import com.lemnt.member.application.growth.converter.MemberGrowthDTOConverter;
import com.lemnt.member.application.growth.dto.SignConfigDTO;
import com.lemnt.member.application.growth.qry.SignConfigDetailQry;
import com.lemnt.member.domain.growth.gateway.MemberSignConfigGateway;
import com.lemnt.member.domain.shared.model.MemberRules;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 签到配置详情查询执行器。 */
@Component
@RequiredArgsConstructor
public class MemberSignConfigDetailQueryExe {

    private final MemberSignConfigGateway memberSignConfigGateway;

    /**
     * 执行签到配置详情查询。
     */
    public SignConfigDTO execute(SignConfigDetailQry qry) {
        MemberRules.id(qry.id());
        return MemberGrowthDTOConverter.toSignConfigDTO(memberSignConfigGateway.signConfig(qry.id()));
    }
}

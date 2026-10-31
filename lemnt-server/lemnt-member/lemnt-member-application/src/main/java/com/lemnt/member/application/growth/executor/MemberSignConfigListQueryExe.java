package com.lemnt.member.application.growth.executor;

import com.lemnt.member.application.growth.converter.MemberGrowthDTOConverter;
import com.lemnt.member.application.growth.dto.SignConfigDTO;
import com.lemnt.member.application.growth.qry.SignConfigListQry;
import com.lemnt.member.domain.growth.gateway.MemberSignConfigGateway;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

/** 签到配置项查询执行器。 */
@Component
@RequiredArgsConstructor
public class MemberSignConfigListQueryExe {

    private final MemberSignConfigGateway memberSignConfigGateway;

    /**
     * 执行签到配置项查询。
     */
    public List<SignConfigDTO> execute(SignConfigListQry qry) {
        return memberSignConfigGateway.signConfigs().stream()
                .filter(v -> !qry.enabledOnly() || v.status() == 1)
                .map(MemberGrowthDTOConverter::toSignConfigDTO)
                .toList();
    }
}

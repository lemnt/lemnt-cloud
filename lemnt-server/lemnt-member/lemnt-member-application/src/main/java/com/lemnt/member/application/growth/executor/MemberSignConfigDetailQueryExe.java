package com.lemnt.member.application.growth.executor;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.member.application.growth.converter.MemberGrowthDTOConverter;
import com.lemnt.member.application.growth.dto.SignConfigDTO;
import com.lemnt.member.application.growth.qry.SignConfigDetailQry;
import com.lemnt.member.domain.growth.gateway.MemberSignConfigGateway;

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
        if (qry.day() == null || qry.day() < 1 || qry.day() > 366) {
            throw new ServiceException("签到天数不合法", 400);
        }
        return memberSignConfigGateway.signConfigs().stream()
                .filter(v -> v.day().equals(qry.day()))
                .findFirst()
                .map(MemberGrowthDTOConverter::toSignConfigDTO)
                .orElse(null);
    }
}

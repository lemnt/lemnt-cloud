package com.lemnt.member.application.growth.executor;

import com.lemnt.member.application.growth.cmd.SignConfigCreateCmd;
import com.lemnt.member.application.growth.service.MemberGrowthService;
import com.lemnt.member.domain.growth.model.SignConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** 签到配置创建执行器。 */
@Component
@RequiredArgsConstructor
public class MemberSignConfigCreateCmdExe {
    private final MemberGrowthService service;
    /** 执行独立创建，不按天数覆盖。 */
    public Long execute(SignConfigCreateCmd cmd) {
        return service.createSignConfig(new SignConfig(cmd.day(), cmd.point(), cmd.experience(), cmd.status()));
    }
}

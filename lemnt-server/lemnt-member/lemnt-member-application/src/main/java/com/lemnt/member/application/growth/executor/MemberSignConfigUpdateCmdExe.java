package com.lemnt.member.application.growth.executor;

import com.lemnt.member.application.growth.cmd.SignConfigUpdateCmd;
import com.lemnt.member.application.growth.service.MemberGrowthService;
import com.lemnt.member.domain.growth.model.SignConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** 签到配置更新执行器。 */
@Component
@RequiredArgsConstructor
public class MemberSignConfigUpdateCmdExe {
    private final MemberGrowthService service;
    /** 执行独立更新，不按天数覆盖。 */
    public Boolean execute(SignConfigUpdateCmd cmd) {
        return service.updateSignConfig(new SignConfig(cmd.id(), cmd.day(), cmd.point(), cmd.experience(), cmd.status()));
    }
}

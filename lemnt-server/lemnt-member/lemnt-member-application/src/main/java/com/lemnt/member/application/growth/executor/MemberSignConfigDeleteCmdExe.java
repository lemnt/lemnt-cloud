package com.lemnt.member.application.growth.executor;

import com.lemnt.member.application.growth.cmd.SignConfigDeleteCmd;
import com.lemnt.member.application.growth.service.MemberGrowthService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 签到配置删除命令执行器。 */
@Component
@RequiredArgsConstructor
public class MemberSignConfigDeleteCmdExe {

    private final MemberGrowthService memberGrowthService;

    /**
     * 执行签到配置删除命令。
     */
    public Boolean execute(SignConfigDeleteCmd cmd) {
        return memberGrowthService.deleteSignConfig(cmd.id());
    }
}

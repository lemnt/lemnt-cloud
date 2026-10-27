package com.lemnt.member.application.growth.executor;

import com.lemnt.member.application.growth.cmd.SignConfigSaveCmd;
import com.lemnt.member.application.growth.service.MemberGrowthService;
import com.lemnt.member.domain.growth.model.SignConfig;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 签到配置保存命令执行器。 */
@Component
@RequiredArgsConstructor
public class MemberSignConfigSaveCmdExe {

    private final MemberGrowthService memberGrowthService;

    /**
     * 执行签到配置保存命令。
     */
    public Boolean execute(SignConfigSaveCmd cmd) {
        return memberGrowthService.saveSignConfig(
                new SignConfig(cmd.day(), cmd.point(), cmd.experience(), cmd.status()));
    }
}

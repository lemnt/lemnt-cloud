package com.lemnt.member.application.growth.executor;

import com.lemnt.member.application.growth.cmd.ConfigSaveCmd;
import com.lemnt.member.application.growth.service.MemberGrowthService;
import com.lemnt.member.domain.growth.model.Config;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 配置保存命令执行器。 */
@Component
@RequiredArgsConstructor
public class MemberConfigSaveCmdExe {

    private final MemberGrowthService memberGrowthService;

    /**
     * 执行配置保存命令。
     */
    public Boolean execute(ConfigSaveCmd cmd) {
        return memberGrowthService.saveConfig(
                new Config(
                        cmd.pointTradeDeductEnable(),
                        cmd.pointTradeDeductUnitPrice(),
                        cmd.pointTradeDeductMaxPrice(),
                        cmd.pointTradeGivePoint()));
    }
}

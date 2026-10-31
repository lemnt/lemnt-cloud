package com.lemnt.member.application.growth.executor;

import com.lemnt.member.application.growth.cmd.LevelSaveCmd;
import com.lemnt.member.application.growth.service.MemberGrowthService;
import com.lemnt.member.domain.growth.model.Level;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 等级保存命令执行器。 */
@Component
@RequiredArgsConstructor
public class MemberLevelSaveCmdExe {

    private final MemberGrowthService memberGrowthService;

    /**
     * 执行等级保存命令。
     */
    public Long execute(LevelSaveCmd cmd) {
        return memberGrowthService.saveLevel(
                new Level(
                        cmd.id(),
                        cmd.name(),
                        cmd.level(),
                        cmd.experience(),
                        cmd.discountPercent(),
                        cmd.icon(),
                        cmd.backgroundUrl(),
                        cmd.status()));
    }
}

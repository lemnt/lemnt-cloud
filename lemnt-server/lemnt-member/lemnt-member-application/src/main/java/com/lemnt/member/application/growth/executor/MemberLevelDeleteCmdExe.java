package com.lemnt.member.application.growth.executor;

import com.lemnt.member.application.growth.cmd.LevelDeleteCmd;
import com.lemnt.member.application.growth.service.MemberGrowthService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 等级删除命令执行器。 */
@Component
@RequiredArgsConstructor
public class MemberLevelDeleteCmdExe {

    private final MemberGrowthService memberGrowthService;

    /**
     * 执行等级删除命令。
     */
    public Boolean execute(LevelDeleteCmd cmd) {
        return memberGrowthService.deleteLevel(cmd.id());
    }
}

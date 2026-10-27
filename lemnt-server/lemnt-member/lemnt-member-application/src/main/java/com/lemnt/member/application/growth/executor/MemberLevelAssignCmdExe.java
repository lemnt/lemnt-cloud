package com.lemnt.member.application.growth.executor;

import com.lemnt.member.application.growth.cmd.LevelAssignCmd;
import com.lemnt.member.application.growth.service.MemberGrowthService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 等级分配命令执行器。 */
@Component
@RequiredArgsConstructor
public class MemberLevelAssignCmdExe {

    private final MemberGrowthService memberGrowthService;

    /**
     * 执行等级分配命令。
     */
    public Boolean execute(LevelAssignCmd cmd) {
        return memberGrowthService.assignLevel(cmd.memberId(), cmd.levelId(), cmd.requestNo(), cmd.reason());
    }
}

package com.lemnt.member.application.user.executor;

import com.lemnt.member.application.user.cmd.GroupAssignCmd;
import com.lemnt.member.application.user.service.MemberUserService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 分组分配命令执行器。 */
@Component
@RequiredArgsConstructor
public class MemberGroupAssignCmdExe {

    private final MemberUserService memberUserService;

    /**
     * 执行分组分配命令。
     */
    public Boolean execute(GroupAssignCmd cmd) {
        return memberUserService.assignGroup(cmd.memberId(), cmd.groupId());
    }
}

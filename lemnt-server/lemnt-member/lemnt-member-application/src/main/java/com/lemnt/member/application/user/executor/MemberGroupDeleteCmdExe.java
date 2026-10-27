package com.lemnt.member.application.user.executor;

import com.lemnt.member.application.user.cmd.GroupDeleteCmd;
import com.lemnt.member.application.user.service.MemberUserService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 分组删除命令执行器。 */
@Component
@RequiredArgsConstructor
public class MemberGroupDeleteCmdExe {

    private final MemberUserService memberUserService;

    /**
     * 执行分组删除命令。
     */
    public Boolean execute(GroupDeleteCmd cmd) {
        return memberUserService.deleteGroup(cmd.id());
    }
}

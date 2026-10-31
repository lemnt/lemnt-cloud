package com.lemnt.member.application.user.executor;

import com.lemnt.member.application.user.cmd.GroupSaveCmd;
import com.lemnt.member.application.user.service.MemberUserService;
import com.lemnt.member.domain.user.model.Group;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 分组保存命令执行器。 */
@Component
@RequiredArgsConstructor
public class MemberGroupSaveCmdExe {

    private final MemberUserService memberUserService;

    /**
     * 执行分组保存命令。
     */
    public Long execute(GroupSaveCmd cmd) {
        return memberUserService.saveGroup(new Group(cmd.id(), cmd.name(), cmd.remark(), cmd.status()));
    }
}

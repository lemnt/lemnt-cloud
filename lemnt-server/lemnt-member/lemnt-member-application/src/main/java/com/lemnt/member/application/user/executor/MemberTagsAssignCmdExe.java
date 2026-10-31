package com.lemnt.member.application.user.executor;

import com.lemnt.member.application.user.cmd.TagsAssignCmd;
import com.lemnt.member.application.user.service.MemberUserService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 查询标签分配命令执行器。 */
@Component
@RequiredArgsConstructor
public class MemberTagsAssignCmdExe {

    private final MemberUserService memberUserService;

    /**
     * 执行会员标签分配命令。
     */
    public Boolean execute(TagsAssignCmd cmd) {
        return memberUserService.assignTags(cmd.memberId(), cmd.tagIds());
    }
}

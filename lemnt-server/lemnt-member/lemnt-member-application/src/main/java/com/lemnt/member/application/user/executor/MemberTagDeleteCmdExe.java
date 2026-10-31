package com.lemnt.member.application.user.executor;

import com.lemnt.member.application.user.cmd.TagDeleteCmd;
import com.lemnt.member.application.user.service.MemberUserService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 标签删除命令执行器。 */
@Component
@RequiredArgsConstructor
public class MemberTagDeleteCmdExe {

    private final MemberUserService memberUserService;

    /**
     * 执行标签删除命令。
     */
    public Boolean execute(TagDeleteCmd cmd) {
        return memberUserService.deleteTag(cmd.id());
    }
}

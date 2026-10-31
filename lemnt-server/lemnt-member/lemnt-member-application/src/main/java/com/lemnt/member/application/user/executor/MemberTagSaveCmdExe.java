package com.lemnt.member.application.user.executor;

import com.lemnt.member.application.user.cmd.TagSaveCmd;
import com.lemnt.member.application.user.service.MemberUserService;
import com.lemnt.member.domain.user.model.Tag;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 标签保存命令执行器。 */
@Component
@RequiredArgsConstructor
public class MemberTagSaveCmdExe {

    private final MemberUserService memberUserService;

    /**
     * 执行标签保存命令。
     */
    public Long execute(TagSaveCmd cmd) {
        return memberUserService.saveTag(new Tag(cmd.id(), cmd.name(), cmd.remark()));
    }
}

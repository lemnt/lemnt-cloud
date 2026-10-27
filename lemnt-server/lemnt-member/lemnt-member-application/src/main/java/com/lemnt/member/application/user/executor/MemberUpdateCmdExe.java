package com.lemnt.member.application.user.executor;

import com.lemnt.member.application.user.cmd.MemberUpdateCmd;
import com.lemnt.member.application.user.service.MemberUserService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 资料更新命令执行器。 */
@Component
@RequiredArgsConstructor
public class MemberUpdateCmdExe {

    private final MemberUserService memberUserService;

    /**
     * 执行资料更新命令。
     */
    public Boolean execute(MemberUpdateCmd cmd) {
        return memberUserService.profileUpdate(
                cmd.memberId(),
                cmd.nickname(),
                cmd.email(),
                cmd.avatar(),
                cmd.sex(),
                cmd.birthday(),
                cmd.status());
    }
}

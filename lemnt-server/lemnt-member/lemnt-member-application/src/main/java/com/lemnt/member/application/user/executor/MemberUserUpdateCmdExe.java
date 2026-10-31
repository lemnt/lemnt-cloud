package com.lemnt.member.application.user.executor;

import com.lemnt.member.application.user.cmd.MemberUserUpdateCmd;
import com.lemnt.member.application.user.service.MemberUserService;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

/** 后台会员资料更新执行器。 */
@Component
@RequiredArgsConstructor
public class MemberUserUpdateCmdExe {

    private final MemberUserService memberUserService;

    /** 在同一事务校验并保存资料及登录手机号。 */
    public Boolean execute(MemberUserUpdateCmd cmd) {
        return memberUserService.updateUser(cmd);
    }
}

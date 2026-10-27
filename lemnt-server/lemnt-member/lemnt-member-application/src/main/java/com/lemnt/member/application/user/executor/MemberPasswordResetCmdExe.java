package com.lemnt.member.application.user.executor;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.common.security.utils.SecurityUtils;
import com.lemnt.member.application.user.cmd.PasswordResetCmd;
import com.lemnt.member.domain.shared.model.MemberRules;
import com.lemnt.member.domain.user.gateway.MemberUserGateway;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/** 会员密码重置命令执行器。 */
@Component
@RequiredArgsConstructor
public class MemberPasswordResetCmdExe {

    private final MemberUserGateway memberUserGateway;

    /** 重置会员密码，校验与写入在同一事务内完成。 */
    @Transactional(rollbackFor = Exception.class)
    public Boolean execute(PasswordResetCmd cmd) {
        MemberRules.id(cmd.memberId());
        MemberRules.password(cmd.password());
        if (memberUserGateway.lockById(cmd.memberId()) == null) {
            throw new ServiceException("会员不存在", 400);
        }
        memberUserGateway.updatePassword(cmd.memberId(), SecurityUtils.encryptPassword(cmd.password()));
        return true;
    }
}

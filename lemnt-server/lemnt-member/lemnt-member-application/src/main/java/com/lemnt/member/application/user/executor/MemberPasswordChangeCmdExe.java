package com.lemnt.member.application.user.executor;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.common.security.utils.SecurityUtils;
import com.lemnt.member.application.user.cmd.PasswordChangeCmd;
import com.lemnt.member.domain.shared.model.MemberRules;
import com.lemnt.member.domain.user.gateway.MemberUserGateway;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/** 会员密码修改命令执行器。 */
@Component
@RequiredArgsConstructor
public class MemberPasswordChangeCmdExe {

    private final MemberUserGateway memberUserGateway;

    /** 校验原密码后更新密码，校验与写入在同一事务内完成。 */
    @Transactional(rollbackFor = Exception.class)
    public Boolean execute(PasswordChangeCmd cmd) {
        var member = memberUserGateway.lockById(cmd.memberId());
        if (member == null || member.status() != 1) {
            throw new ServiceException("会员会话无效", 401);
        }
        MemberRules.password(cmd.newPassword());
        String password = memberUserGateway.findPasswordByMemberId(cmd.memberId());
        if (cmd.oldPassword() == null || password == null
                || !SecurityUtils.matchesPassword(cmd.oldPassword(), password)) {
            throw new ServiceException("原密码错误", 401);
        }
        memberUserGateway.updatePassword(cmd.memberId(), SecurityUtils.encryptPassword(cmd.newPassword()));
        return true;
    }
}

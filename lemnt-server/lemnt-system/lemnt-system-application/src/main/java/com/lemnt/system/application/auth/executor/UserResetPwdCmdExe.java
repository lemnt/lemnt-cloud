package com.lemnt.system.application.auth.executor;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.system.application.auth.cmd.UserPwdResetCmd;
import com.lemnt.system.domain.auth.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import com.lemnt.common.security.utils.SecurityUtils;
import com.lemnt.system.domain.auth.gateway.UserGateway;

/**
 * 重置用户密码命令执行器
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class UserResetPwdCmdExe {
    private final UserGateway userGateway;

    public Boolean execute(UserPwdResetCmd cmd) {
        User user = new User();
        user.setUserId(cmd.userId());
        user.setPassword(SecurityUtils.encryptPassword(cmd.password()));
        if (user.isAdmin()) {
            throw new ServiceException("不允许操作超级管理员用户");
        }
        return userGateway.resetUserPwd(user.getUserId(), user.getPassword());
    }
}

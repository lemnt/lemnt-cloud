package com.lemnt.system.application.auth.executor;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.system.application.auth.cmd.UserStatusUpdateCmd;
import com.lemnt.system.domain.auth.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import com.lemnt.system.domain.auth.gateway.UserGateway;

/**
 * 修改用户状态命令执行器
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class UserUpdateStatusCmdExe {

    private final UserGateway userGateway;

    public Boolean execute(UserStatusUpdateCmd cmd) {
        User user = new User();
        user.setUserId(cmd.userId());
        user.setStatus(cmd.status());
        if (user.isAdmin()) {
            throw new ServiceException("不允许操作超级管理员用户");
        }
        return userGateway.updateUserStatus(user);
    }
}

package com.lemnt.system.application.auth.executor;

import com.lemnt.common.security.utils.SecurityUtils;
import com.lemnt.system.application.auth.cmd.UserPwdUpdateCmd;
import com.lemnt.system.domain.auth.gateway.UserGateway;
import com.lemnt.system.domain.auth.model.User;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 用户修改个人密码命令执行器
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class UserUpdatePwdCmdExe {
    private final UserGateway userGateway;
    public Boolean execute(UserPwdUpdateCmd cmd) {
        String username = SecurityUtils.getUsername();
        User user = userGateway.selectUserByUserName(username);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        // 校验旧密码
        if (!SecurityUtils.matchesPassword(cmd.oldPassword(), user.getPassword())) {
            throw new RuntimeException("修改密码失败，旧密码错误");
        }

        // 新密码不能与旧密码相同
        if (SecurityUtils.matchesPassword(cmd.newPassword(), user.getPassword())) {
            throw new RuntimeException("新密码不能与旧密码相同");
        }

        // 重置密码
        return userGateway.resetUserPwd(user.getUserId(), SecurityUtils.encryptPassword(cmd.newPassword()));
    }
}

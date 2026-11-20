package com.lemnt.system.application.auth.executor;

import com.lemnt.common.security.utils.SecurityUtils;
import com.lemnt.system.application.auth.cmd.UserProfileUpdateCmd;
import com.lemnt.system.domain.auth.gateway.UserGateway;
import com.lemnt.system.domain.auth.model.User;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 修改个人资料命令执行器
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class UserUpdateProfileCmdExe {
    private final UserGateway userGateway;

    /**
     * 执行修改个人资料
     *
     * @return 结果
     */
    public Boolean execute(UserProfileUpdateCmd cmd) {
        User entity = new User();
        entity.setUserId(SecurityUtils.getUserId());
        entity.setNickName(cmd.nickName());
        entity.setPhonenumber(cmd.phonenumber());
        entity.setEmail(cmd.email());
        entity.setSex(cmd.sex());
        entity.setUpdateBy(SecurityUtils.getUsername());
        return userGateway.updateUser(entity);
    }
}

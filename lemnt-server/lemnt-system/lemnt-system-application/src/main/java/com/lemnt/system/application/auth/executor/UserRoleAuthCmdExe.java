package com.lemnt.system.application.auth.executor;

import com.lemnt.system.application.auth.cmd.UserRoleAuthCmd;
import com.lemnt.system.domain.auth.gateway.UserRoleGateway;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/**
 * 用户授权角色命令执行器
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class UserRoleAuthCmdExe {
    private final UserRoleGateway userRoleGateway;

    @Transactional(rollbackFor = Exception.class)
    public Boolean execute(UserRoleAuthCmd cmd) {
        // 删除用户原有角色
        userRoleGateway.deleteUserRoleByUserId(cmd.userId());

        // 新增用户角色关联
        userRoleGateway.insertUserRoles(cmd.userId(), cmd.roleIds());
        return true;
    }
}

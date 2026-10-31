package com.lemnt.system.application.auth.executor;

import com.lemnt.system.application.auth.cmd.RoleAuthCancelCmd;
import com.lemnt.system.domain.auth.gateway.UserRoleGateway;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/**
 * 角色批量授权命令执行器
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class RoleAuthCancelBatchCmdExe {
    private final UserRoleGateway userRoleGateway;

    @Transactional(rollbackFor = Exception.class)
    public Boolean execute(RoleAuthCancelCmd cmd) {
        return userRoleGateway.deleteUserRoleInfosByRoleId(cmd.roleId(), cmd.userIds());
    }
}

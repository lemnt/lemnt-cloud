package com.lemnt.system.application.auth.executor;

import com.lemnt.system.application.auth.cmd.RoleAuthCancelCmd;
import com.lemnt.system.domain.auth.gateway.UserGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 角色取消用户授权命令执行器
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class RoleAuthCancelCmdExe {

    private final UserGateway userGateway;

    @Transactional(rollbackFor = Exception.class)
    public Boolean execute(RoleAuthCancelCmd cmd) {
        return userGateway.deleteUserRoleInfo(cmd.userId(), cmd.roleId());
    }
}

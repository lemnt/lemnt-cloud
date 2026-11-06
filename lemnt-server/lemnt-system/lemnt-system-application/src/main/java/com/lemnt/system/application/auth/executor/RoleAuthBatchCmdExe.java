package com.lemnt.system.application.auth.executor;

import com.lemnt.system.application.auth.cmd.RoleAuthCmd;
import com.lemnt.system.domain.auth.gateway.UserRoleGateway;
import com.lemnt.system.domain.auth.service.RoleDomainService;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/**
 * 角色授权命令执行器（批量授权/取消授权）
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class RoleAuthBatchCmdExe {
    private final UserRoleGateway userRoleGateway;
    private final RoleDomainService roleDomainService;

    @Transactional(rollbackFor = Exception.class)
    public Boolean execute(RoleAuthCmd cmd) {
        roleDomainService.checkRoleDataScope(cmd.roleId());
        userRoleGateway.insertUserRoles(cmd.roleId(), cmd.userIds());
        return true;
    }
}

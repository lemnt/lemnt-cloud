package com.lemnt.system.application.auth.executor;

import com.lemnt.system.application.auth.cmd.RoleAuthCmd;
import com.lemnt.system.domain.auth.service.RoleDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.lemnt.system.domain.auth.gateway.UserGateway;

/**
 * 角色授权命令执行器（批量授权/取消授权）
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class RoleAuthBatchCmdExe {

    private final UserGateway userGateway;
    private final RoleDomainService roleDomainService;

    @Transactional(rollbackFor = Exception.class)
    public Boolean execute(RoleAuthCmd cmd) {
        roleDomainService.checkRoleDataScope(cmd.roleId());
        userGateway.insertUserRoles(cmd.roleId(), cmd.userIds());
        return true;
    }
}

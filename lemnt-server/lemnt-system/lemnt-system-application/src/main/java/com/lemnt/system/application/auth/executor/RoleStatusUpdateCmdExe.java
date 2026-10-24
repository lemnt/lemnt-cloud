package com.lemnt.system.application.auth.executor;

import com.lemnt.common.security.utils.SecurityUtils;
import com.lemnt.system.application.auth.cmd.RoleStatusUpdateCmd;
import com.lemnt.system.domain.auth.service.RoleDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import com.lemnt.system.domain.auth.model.Role;
import com.lemnt.system.domain.auth.gateway.RoleGateway;

/**
 * 角色状态修改命令执行器
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class RoleStatusUpdateCmdExe {

    private final RoleDomainService roleDomainService;
    private final RoleGateway roleGateway;

    public Boolean execute(RoleStatusUpdateCmd cmd) {
        Role entity = new Role();
        entity.setRoleId(cmd.roleId());
        entity.setStatus(cmd.status());
        roleDomainService.checkRoleAllowed(entity);
        if (!SecurityUtils.isAdmin()) {
            roleDomainService.checkRoleDataScope(entity.getRoleId());
        }
        return roleGateway.updateRole(entity);
    }
}

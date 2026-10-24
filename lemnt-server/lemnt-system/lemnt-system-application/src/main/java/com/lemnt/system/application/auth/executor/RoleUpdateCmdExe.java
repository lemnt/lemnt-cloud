package com.lemnt.system.application.auth.executor;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.lemnt.system.application.auth.cmd.RoleUpdateCmd;
import com.lemnt.system.domain.auth.model.Role;
import com.lemnt.system.domain.auth.gateway.RoleGateway;
import com.lemnt.system.domain.auth.service.RoleDomainService;

/**
 * 修改角色命令执行器
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class RoleUpdateCmdExe {

    private final RoleGateway roleGateway;
    private final RoleDomainService roleDomainService;

    @Transactional(rollbackFor = Exception.class)
    public Boolean execute(RoleUpdateCmd cmd) {
        Role entity = new Role();
        entity.setRoleId(cmd.roleId());
        entity.setRoleName(cmd.roleName());
        entity.setRoleKey(cmd.roleKey());
        entity.setRoleSort(cmd.roleSort());
        entity.setDataScope(cmd.dataScope());
        entity.setStatus(cmd.status());
        roleDomainService.checkRoleAllowed(entity);
        roleDomainService.checkRoleDataScope(entity.getRoleId());
        roleDomainService.checkRoleNameUnique(cmd.roleId(), cmd.roleName());
        roleDomainService.checkRoleKeyUnique(cmd.roleId(), cmd.roleKey());
        // 修改角色菜单关联（先删后插）
        roleGateway.deleteRoleMenuByRoleId(cmd.roleId());
        roleGateway.insertRoleMenus(cmd.roleId(), cmd.menuIds());
        return roleGateway.updateRole(entity);
    }
}

package com.lemnt.system.application.auth.executor;

import com.lemnt.system.application.auth.cmd.RoleAddCmd;
import com.lemnt.system.domain.auth.gateway.RoleGateway;
import com.lemnt.system.domain.auth.gateway.RoleMenuGateway;
import com.lemnt.system.domain.auth.model.Role;
import com.lemnt.system.domain.auth.service.RoleDomainService;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/**
 * 新增角色命令执行器
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class RoleAddCmdExe {
    private final RoleMenuGateway roleMenuGateway;
    private final RoleGateway roleGateway;
    private final RoleDomainService roleDomainService;

    /**
     * 执行新增角色
     *
     * @param cmd 新增命令
     * @return 结果
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean execute(RoleAddCmd cmd) {
        Role entity = new Role();
        entity.setRoleName(cmd.roleName());
        entity.setRoleKey(cmd.roleKey());
        entity.setRoleSort(cmd.roleSort());
        entity.setDataScope(cmd.dataScope());
        entity.setStatus(cmd.status());
        roleDomainService.checkRoleNameUnique(cmd.roleName());
        roleDomainService.checkRoleKeyUnique(cmd.roleKey());
        Boolean result = roleGateway.insertRole(entity);
        if (result) {
            // 新增角色菜单关联
            roleMenuGateway.insertRoleMenus(entity.getRoleId(), cmd.menuIds());
        }
        return result;
    }
}

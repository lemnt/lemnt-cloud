package com.lemnt.system.application.auth.executor;

import com.lemnt.common.security.utils.SecurityUtils;
import com.lemnt.system.application.auth.cmd.RoleDataScopeUpdateCmd;
import com.lemnt.system.domain.auth.gateway.RoleGateway;
import com.lemnt.system.domain.auth.model.Role;
import com.lemnt.system.domain.auth.service.RoleDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 角色数据权限范围修改命令执行器
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class RoleDataScopeUpdateCmdExe {

    private final RoleDomainService roleDomainService;
    private final RoleGateway roleGateway;

    public Boolean execute(RoleDataScopeUpdateCmd cmd) {
        Role entity = new Role();
        entity.setRoleId(cmd.roleId());
        entity.setDataScope(cmd.dataScope());
        roleDomainService.checkRoleAllowed(entity);
        if (!SecurityUtils.isAdmin()) {
            roleDomainService.checkRoleDataScope(entity.getRoleId());
        }
        // 修改角色信息
        roleGateway.updateRole(entity);
        // 删除角色与部门关联
        roleGateway.deleteRoleDeptByRoleId(entity.getRoleId());
        // 新增角色和部门信息（数据权限）
        return roleGateway.insertRoleDepts(entity.getRoleId(), cmd.deptIds());
    }
}

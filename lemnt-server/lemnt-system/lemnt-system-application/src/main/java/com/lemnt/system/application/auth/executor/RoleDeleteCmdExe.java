package com.lemnt.system.application.auth.executor;

import com.lemnt.system.domain.auth.gateway.RoleDeptGateway;
import com.lemnt.system.domain.auth.gateway.RoleGateway;
import com.lemnt.system.domain.auth.gateway.RoleMenuGateway;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * 删除角色命令执行器
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class RoleDeleteCmdExe {
    private final RoleMenuGateway roleMenuGateway;
    private final RoleDeptGateway roleDeptGateway;
    private final RoleGateway roleGateway;

    /**
     * 执行批量删除角色（先删关联，再删角色）
     *
     * @param roleIds 角色ID数组
     * @return 结果
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean execute(List<Long> roleIds) {
        // 删除角色与菜单关联
        roleMenuGateway.deleteRoleMenu(roleIds);

        // 删除角色与部门关联
        roleDeptGateway.deleteRoleDept(roleIds);
        return roleGateway.deleteRoleByIds(roleIds);
    }
}

package com.lemnt.system.application.auth.executor;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.lemnt.system.domain.auth.gateway.RoleGateway;

import java.util.List;

/**
 * 删除角色命令执行器
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class RoleDeleteCmdExe {
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
        roleGateway.deleteRoleMenu(roleIds);
        // 删除角色与部门关联
        roleGateway.deleteRoleDept(roleIds);
        return roleGateway.deleteRoleByIds(roleIds);
    }
}

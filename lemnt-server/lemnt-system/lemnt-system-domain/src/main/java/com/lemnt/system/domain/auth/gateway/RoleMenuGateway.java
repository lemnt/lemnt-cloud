package com.lemnt.system.domain.auth.gateway;

import java.util.List;

/** RoleMenu 存储接口。 */
public interface RoleMenuGateway {
    /**
     * 通过角色ID删除角色菜单关联
     *
     * @param roleId 角色ID
     * @return 是否成功
     */
    Boolean deleteRoleMenuByRoleId(Long roleId);

    /**
     * 批量删除角色菜单关联
     *
     * @param ids 角色ID数组
     * @return 是否成功
     */
    Boolean deleteRoleMenu(List<Long> ids);

    /**
     * 批量新增角色菜单
     *
     * @param roleId  角色ID
     * @param menuIds 菜单ID数组
     * @return 是否成功
     */
    Boolean insertRoleMenus(Long roleId, List<Long> menuIds);
}

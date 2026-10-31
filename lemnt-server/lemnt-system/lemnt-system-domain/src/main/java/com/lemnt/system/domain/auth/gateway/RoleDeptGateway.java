package com.lemnt.system.domain.auth.gateway;

import java.util.List;

/** RoleDept 存储接口。 */
public interface RoleDeptGateway {
    /**
     * 通过角色ID删除角色部门关联
     *
     * @param roleId 角色ID
     * @return 是否成功
     */
    Boolean deleteRoleDeptByRoleId(Long roleId);

    /**
     * 批量删除角色部门关联
     *
     * @param ids 角色ID数组
     * @return 是否成功
     */
    Boolean deleteRoleDept(List<Long> ids);

    /**
     * 批量新增角色部门
     *
     * @param roleId  角色ID
     * @param deptIds 部门ID数组
     * @return 是否成功
     */
    Boolean insertRoleDepts(Long roleId, List<Long> deptIds);
    /** 统计部门关联的角色数量。 */
    Long selectCountRoleDeptByDeptId(Long deptId);
}

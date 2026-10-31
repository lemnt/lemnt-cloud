package com.lemnt.system.domain.auth.gateway;

import java.util.List;

/** UserRole 存储接口。 */
public interface UserRoleGateway {
    /**
     * 新增用户角色关联
     *
     * @param userId  用户ID
     * @param roleIds 角色ID数组
     */
    void insertUserRoles(Long userId, List<Long> roleIds);

    /**
     * 删除用户角色关联
     *
     * @param userId 用户ID
     * @return 是否成功
     */
    Boolean deleteUserRoleByUserId(Long userId);

    /**
     * 删除单个用户角色关联
     *
     * @param userId 用户ID
     * @param roleId 角色ID
     * @return 是否成功
     */
    Boolean deleteUserRoleInfo(Long userId, Long roleId);

    /**
     * 批量取消授权（同一角色下多个用户）
     *
     * @param roleId  角色ID
     * @param userIds 用户ID数组
     * @return 是否成功
     */
    Boolean deleteUserRoleInfosByRoleId(Long roleId, List<Long> userIds);

    /**
     * 批量删除用户角色关联
     *
     * @param userIds 用户ID数组
     * @return 是否成功
     */
    Boolean deleteUserRoleByUserIds(List<Long> userIds);
}

package com.lemnt.system.domain.auth.gateway;

import java.util.List;
import java.util.Set;

import com.lemnt.common.domain.model.PageQry;
import com.lemnt.common.domain.model.PageResult;
import com.lemnt.system.domain.auth.model.Role;

/**
 * 角色管理 Gateway 端口
 *
 * @author wangxx
 */
public interface RoleGateway {

    /**
     * 根据条件分页查询角色列表
     *
     * @param role    角色信息
     * @param pageQry 分页参数
     * @return 分页结果
     */
    PageResult<Role> selectPage(Role role, PageQry pageQry);

    /**
     * 根据条件查询角色列表
     *
     * @param role 角色信息
     * @return 角色列表
     */
    List<Role> selectRoleList(Role role);

    /**
     * 根据用户ID查询角色
     *
     * @param userId 用户ID
     * @return 角色列表
     */
    List<Role> selectRolesByUserId(Long userId);

    /**
     * 根据用户ID查询角色
     *
     * @param userId 用户ID
     * @return 角色列表
     */
    Set<String> selectRolePermissionByUserId(Long userId);

    /**
     * 查询所有角色
     *
     * @return 角色列表
     */
    List<Role> selectRoleAll();

    /**
     * 根据用户ID获取角色ID列表
     *
     * @param userId 用户ID
     * @return 角色ID列表
     */
    List<Long> selectRoleListByUserId(Long userId);

    /**
     * 通过角色ID查询角色
     *
     * @param roleId 角色ID
     * @return 角色信息
     */
    Role selectRoleById(Long roleId);

    /**
     * 根据用户名查询角色
     *
     * @param userName 用户名
     * @return 角色列表
     */
    List<Role> selectRolesByUserName(String userName);

    /**
     * 校验角色名称是否唯一
     *
     * @param roleName 角色名称
     * @return 角色信息
     */
    Role checkRoleNameUnique(String roleName);

    /**
     * 校验角色权限是否唯一
     *
     * @param roleKey 角色权限
     * @return 角色信息
     */
    Role checkRoleKeyUnique(String roleKey);

    /**
     * 修改角色
     *
     * @param role 角色信息
     * @return 是否成功
     */
    Boolean updateRole(Role role);

    /**
     * 新增角色
     *
     * @param role 角色信息
     * @return 是否成功
     */
    Boolean insertRole(Role role);

    /**
     * 通过角色ID删除角色
     *
     * @param roleId 角色ID
     * @return 是否成功
     */
    Boolean deleteRoleById(Long roleId);

    /**
     * 批量删除角色
     *
     * @param roleIds 角色ID数组
     * @return 是否成功
     */
    Boolean deleteRoleByIds(List<Long> roleIds);

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

    /**
     * 查询部门被角色使用数量
     *
     * @param deptId 部门ID
     * @return 结果
     */
    Long selectCountRoleDeptByDeptId(Long deptId);
}

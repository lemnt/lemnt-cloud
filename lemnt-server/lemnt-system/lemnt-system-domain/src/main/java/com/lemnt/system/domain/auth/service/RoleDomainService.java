package com.lemnt.system.domain.auth.service;

import cn.hutool.core.collection.CollUtil;
import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.system.domain.auth.model.Role;
import com.lemnt.system.domain.auth.gateway.RoleGateway;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Objects;

/**
 * 角色领域服务 — 封装角色唯一性等业务规则
 *
 * @author wangxx
 */
@RequiredArgsConstructor
public class RoleDomainService {

    private final RoleGateway roleGateway;

    /**
     * 校验角色名称是否唯一（新增场景，不排除自身）
     *
     * @param roleName 角色名称
     * @throws ServiceException 已存在时抛出
     */
    public void checkRoleNameUnique(String roleName) {
        if (roleGateway.checkRoleNameUnique(roleName) != null) {
            throw new ServiceException("角色名称'" + roleName + "'已存在");
        }
    }

    /**
     * 校验角色名称是否唯一（修改场景，排除自身 roleId）
     *
     * @param roleId   当前角色 ID
     * @param roleName 角色名称
     * @throws ServiceException 已存在时抛出
     */
    public void checkRoleNameUnique(Long roleId, String roleName) {
        Role exist = roleGateway.checkRoleNameUnique(roleName);
        if (exist != null && !exist.getRoleId().equals(roleId)) {
            throw new ServiceException("角色名称'" + roleName + "'已存在");
        }
    }

    /**
     * 校验角色权限字符是否唯一（新增场景，不排除自身）
     *
     * @param roleKey 权限字符
     * @throws ServiceException 已存在时抛出
     */
    public void checkRoleKeyUnique(String roleKey) {
        if (roleGateway.checkRoleKeyUnique(roleKey) != null) {
            throw new ServiceException("角色权限字符'" + roleKey + "'已存在");
        }
    }

    /**
     * 校验角色权限字符是否唯一（修改场景，排除自身 roleId）
     *
     * @param roleId  当前角色 ID
     * @param roleKey 权限字符
     * @throws ServiceException 已存在时抛出
     */
    public void checkRoleKeyUnique(Long roleId, String roleKey) {
        Role exist = roleGateway.checkRoleKeyUnique(roleKey);
        if (exist != null && !exist.getRoleId().equals(roleId)) {
            throw new ServiceException("角色权限字符'" + roleKey + "'已存在");
        }
    }

    /**
     * 校验角色是否允许操作
     *
     * @param role 角色信息
     */
    public void checkRoleAllowed(Role role) {
        if (Objects.nonNull(role.getRoleId()) && role.isAdmin()) {
            throw new ServiceException("不允许操作超级管理员角色");
        }
    }

    /**
     * 校验角色是否有数据权限
     *
     * @param roleIds 角色id
     */
    public void checkRoleDataScope(Long... roleIds) {
        for (Long roleId : roleIds) {
            Role role = new Role();
            role.setRoleId(roleId);
            List<Role> roles = roleGateway.selectRoleList(role);
            if (CollUtil.isEmpty(roles)) {
                throw new ServiceException("没有权限访问角色数据！");
            }
        }
    }
}

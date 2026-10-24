package com.lemnt.system.application.auth.executor;

import java.util.*;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import com.lemnt.common.core.constant.Constants;
import com.lemnt.common.security.utils.SecurityUtils;
import com.lemnt.system.domain.auth.gateway.MenuGateway;
import com.lemnt.system.domain.auth.gateway.RoleGateway;
import com.lemnt.system.domain.auth.gateway.UserGateway;

/**
 * 用户信息聚合查询执行器（前端路由初始化）
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class UserInfoQueryExe {

    private final UserGateway userGateway;
    private final RoleGateway roleGateway;
    private final MenuGateway menuGateway;

    /**
     * 获取用户信息（包含角色和权限）
     *
     * @return 用户信息、角色集合、权限集合
     */
    public Map<String, Object> execute() {
        Long userId = SecurityUtils.getUserId();
        // 角色集合
        Set<String> roles = buildRoles(userId);
        // 权限集合
        Set<String> permissions = buildPermissions(userId);
        // 组装结果
        Map<String, Object> result = new HashMap<>();
        result.put("user", userGateway.selectUserById(userId));
        result.put("roles", roles);
        result.put("permissions", permissions);
        return result;
    }

    /**
     * 构建角色集合
     */
    private Set<String> buildRoles(Long userId) {
        Set<String> roles = new HashSet<>();
        if (SecurityUtils.isAdmin()) {
            roles.add(Constants.SUPER_ADMIN);
        } else {
            roles = roleGateway.selectRolePermissionByUserId(userId);
        }
        return roles;
    }

    /**
     * 构建权限集合
     */
    private Set<String> buildPermissions(Long userId) {
        Set<String> permissions = new java.util.HashSet<>();
        if (SecurityUtils.isAdmin()) {
            permissions.add(Constants.ALL_PERMISSION);
        } else {
            permissions.addAll(menuGateway.selectMenuPermsByUserId(userId));
        }
        return permissions;
    }
}

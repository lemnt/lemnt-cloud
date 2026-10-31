package com.lemnt.system.application.auth.executor;

import com.lemnt.common.security.utils.SecurityUtils;
import com.lemnt.system.application.auth.converter.RoleConverter;
import com.lemnt.system.application.auth.converter.UserConverter;
import com.lemnt.system.domain.auth.gateway.RoleGateway;
import com.lemnt.system.domain.auth.gateway.UserGateway;
import com.lemnt.system.domain.auth.model.Role;
import com.lemnt.system.domain.auth.model.User;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户授权角色查询执行器
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class UserRoleAuthQueryExe {
    private final RoleGateway roleGateway;
    private final UserGateway userGateway;

    /**
     * 查询用户可授权角色列表
     *
     * @param userId 用户ID
     * @return 用户信息和可授权角色列表
     */
    public Map<String, Object> execute(Long userId) {
        User user = userGateway.selectUserById(userId);
        List<Role> roles = roleGateway.selectRolesByUserId(userId);
        List<Role> filteredRoles = SecurityUtils.isAdmin(userId) ? roles : roles.stream().filter(r -> !r.isAdmin()).collect(Collectors.toList());
        Map<String, Object> result = new HashMap<>();
        result.put("user", UserConverter.toDTO(user));
        result.put("roles", RoleConverter.toDTOList(filteredRoles));
        return result;
    }
}

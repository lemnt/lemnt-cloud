package com.lemnt.system.application.dubbo;

import com.lemnt.common.security.utils.SecurityUtils;
import com.lemnt.system.api.RemoteUserService;
import com.lemnt.system.api.domain.bo.UserBo;
import com.lemnt.system.api.domain.dto.LoginUserDTO;
import com.lemnt.system.api.domain.dto.UserInfoDTO;
import com.lemnt.system.domain.auth.gateway.MenuGateway;
import com.lemnt.system.domain.auth.gateway.RoleGateway;
import com.lemnt.system.domain.auth.gateway.UserGateway;
import com.lemnt.system.domain.auth.gateway.UserPostGateway;
import com.lemnt.system.domain.auth.gateway.UserRoleGateway;
import com.lemnt.system.domain.auth.model.Role;
import com.lemnt.system.domain.auth.model.User;

import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 用户远程服务实现
 *
 * @author wangxx
 */
@Slf4j
@Component
@DubboService
@RequiredArgsConstructor
public class RemoteUserServiceImpl implements RemoteUserService {
    private final UserPostGateway userPostGateway;
    private final UserRoleGateway userRoleGateway;
    private final UserGateway userGateway;
    private final RoleGateway roleGateway;
    private final MenuGateway menuGateway;

    @Override
    public LoginUserDTO getUserInfo(String username) {
        User sysUser = userGateway.selectUserByUserName(username);
        if (sysUser == null) {
            return null;
        }

        // 角色集合
        Set<String> roles = new HashSet<>();
        if (sysUser.isAdmin()) {
            roles.add("admin");
        } else {
            roles = roleGateway.selectRolePermissionByUserId(sysUser.getUserId());
        }
        // 权限集合
        Set<String> permissions = new HashSet<>();
        if (sysUser.isAdmin()) {
            permissions.add("*:*:*");
        } else {
            permissions.addAll(menuGateway.selectMenuPermsByUserId(sysUser.getUserId()));
        }

        // 构建返回
        LoginUserDTO dto = new LoginUserDTO();
        dto.setUserId(sysUser.getUserId());
        dto.setUsername(sysUser.getUserName());
        dto.setRoleKey(resolveRoleKey(roles));
        dto.setRoles(roles);
        dto.setPermissions(permissions);
        dto.setDetail(buildUserInfo(sysUser));
        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createUser(UserBo bo) {
        if (userGateway.checkUserNameUnique(bo.getUserName()) != null) {
            throw new IllegalArgumentException("用户账号已存在");
        }
        Role role = roleGateway.checkRoleKeyUnique(bo.getRoleKey());
        if (role == null) {
            throw new IllegalArgumentException("用户角色不存在: " + bo.getRoleKey());
        }
        User entity = new User();
        entity.setUserName(bo.getUserName());
        entity.setNickName(bo.getNickName());
        entity.setEmail(bo.getEmail());
        entity.setPhonenumber(bo.getPhonenumber());
        entity.setPassword(SecurityUtils.encryptPassword(bo.getPassword()));
        entity.setStatus("1");
        if (!userGateway.insertUser(entity)) {
            throw new IllegalStateException("创建用户失败");
        }
        userRoleGateway.insertUserRoles(entity.getUserId(), List.of(role.getRoleId()));
        return entity.getUserId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteUser(Long userId) {
        userRoleGateway.deleteUserRoleByUserId(userId);
        userPostGateway.deleteUserPostByUserId(userId);
        return userGateway.deleteUserById(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateUserStatus(List<Long> userIds, String status) {
        for (Long userId : userIds) {
            User user = new User();
            user.setUserId(userId);
            user.setStatus(status);
            if (!userGateway.updateUserStatus(user)) {
                throw new IllegalStateException("修改用户状态失败: " + userId);
            }
        }
        return true;
    }

    @Override
    public Boolean recordUserLogin(UserBo sysUser) {
        User entity = new User();
        entity.setUserName(sysUser.getUserName());
        entity.setLoginIp(sysUser.getLoginIp());
        entity.setLoginDate(sysUser.getLoginDate());
        return userGateway.updateLoginInfo(entity);
    }

    private UserInfoDTO buildUserInfo(User user) {
        UserInfoDTO dto = new UserInfoDTO();
        dto.setUserId(user.getUserId());
        dto.setDeptId(user.getDeptId());
        dto.setUserName(user.getUserName());
        dto.setNickName(user.getNickName());
        dto.setEmail(user.getEmail());
        dto.setPhonenumber(user.getPhonenumber());
        dto.setPassword(user.getPassword());
        dto.setSex(user.getSex());
        dto.setAvatar(user.getAvatar());
        dto.setStatus(user.getStatus());
        dto.setLoginIp(user.getLoginIp());
        dto.setLoginDate(user.getLoginDate());
        return dto;
    }

    private String resolveRoleKey(Set<String> roles) {
        if (roles.contains("admin")) {
            return "admin";
        }
        return roles.stream().findFirst().orElse(null);
    }
}

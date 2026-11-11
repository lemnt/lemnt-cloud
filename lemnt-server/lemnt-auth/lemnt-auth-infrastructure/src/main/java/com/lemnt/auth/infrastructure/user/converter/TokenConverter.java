package com.lemnt.auth.infrastructure.user.converter;

import com.lemnt.auth.domain.user.entity.LoginUser;
import com.lemnt.auth.domain.user.entity.UserInfo;
import com.lemnt.auth.domain.user.entity.UserInfoRole;
import com.lemnt.common.security.model.SecurityRole;
import com.lemnt.common.security.model.SecurityUser;
import com.lemnt.common.security.model.SecurityUserDetail;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Token Gateway 领域实体与通用安全模型转换器
 *
 * @author wangxx
 */
public class TokenConverter {
    /**
     * 领域 LoginUser → 通用安全 SecurityUser
     */
    public static SecurityUser toCommon(LoginUser domain) {
        if (domain == null) {
            return null;
        }

        SecurityUser securityUser = new SecurityUser();
        securityUser.setToken(domain.getToken());
        securityUser.setUserId(domain.getUserId());
        securityUser.setUsername(domain.getUsername());
        securityUser.setLoginTime(domain.getLoginTime());
        securityUser.setExpireTime(domain.getExpireTime());
        securityUser.setIpaddr(domain.getIpaddr());
        securityUser.setRoleKey(domain.getRoleKey());
        if (domain.getPermissions() != null) {
            securityUser.setPermissions(new LinkedHashSet<>(domain.getPermissions()));
        }
        if (domain.getRoles() != null) {
            securityUser.setRoles(new LinkedHashSet<>(domain.getRoles()));
        }
        securityUser.setDetail(toCommon(domain.getDetail()));
        return securityUser;
    }

    /**
     * 通用安全 SecurityUser → 领域 LoginUser
     */
    public static LoginUser toDomain(SecurityUser common) {
        if (common == null) {
            return null;
        }

        LoginUser loginUser = new LoginUser();
        loginUser.setToken(common.getToken());
        loginUser.setUserId(common.getUserId());
        loginUser.setUsername(common.getUsername());
        loginUser.setLoginTime(common.getLoginTime());
        loginUser.setExpireTime(common.getExpireTime());
        loginUser.setIpaddr(common.getIpaddr());
        loginUser.setRoleKey(common.getRoleKey());
        if (common.getPermissions() != null) {
            loginUser.setPermissions(new LinkedHashSet<>(common.getPermissions()));
        }
        if (common.getRoles() != null) {
            loginUser.setRoles(new LinkedHashSet<>(common.getRoles()));
        }
        loginUser.setDetail(toDomain(common.getDetail()));
        return loginUser;
    }

    /**
     * 领域 UserInfo → 通用安全 SecurityUserDetail
     */
    public static SecurityUserDetail toCommon(UserInfo domain) {
        if (domain == null) {
            return null;
        }

        SecurityUserDetail detail = new SecurityUserDetail();
        detail.setUserName(domain.getUserName());
        detail.setUserId(domain.getUserId());
        detail.setDeptId(domain.getDeptId());
        detail.setNickName(domain.getNickName());
        detail.setEmail(domain.getEmail());
        detail.setPhonenumber(domain.getPhonenumber());
        detail.setSex(domain.getSex());
        detail.setAvatar(domain.getAvatar());
        detail.setPassword(domain.getPassword());
        detail.setStatus(domain.getStatus());
        detail.setLoginIp(domain.getLoginIp());
        detail.setLoginDate(domain.getLoginDate());
        detail.setPwdUpdateDate(domain.getPwdUpdateDate());
        detail.setRoles(toRoleList(domain.getRoles()));
        return detail;
    }

    /**
     * 通用安全 SecurityUserDetail → 领域 UserInfo
     */
    public static UserInfo toDomain(SecurityUserDetail detail) {
        if (detail == null) {
            return null;
        }

        UserInfo userInfo = new UserInfo();
        userInfo.setUserName(detail.getUserName());
        userInfo.setUserId(detail.getUserId());
        userInfo.setDeptId(detail.getDeptId());
        userInfo.setNickName(detail.getNickName());
        userInfo.setEmail(detail.getEmail());
        userInfo.setPhonenumber(detail.getPhonenumber());
        userInfo.setSex(detail.getSex());
        userInfo.setAvatar(detail.getAvatar());
        userInfo.setPassword(detail.getPassword());
        userInfo.setStatus(detail.getStatus());
        userInfo.setLoginIp(detail.getLoginIp());
        userInfo.setLoginDate(detail.getLoginDate());
        userInfo.setPwdUpdateDate(detail.getPwdUpdateDate());
        userInfo.setRoles(toDomainList(detail.getRoles()));
        return userInfo;
    }

    /**
     * 领域 UserInfoRole → 通用安全 SecurityRole
     */
    public static SecurityRole toCommon(UserInfoRole domain) {
        if (domain == null) {
            return null;
        }

        SecurityRole securityRole = new SecurityRole();
        securityRole.setRoleId(domain.getRoleId());
        securityRole.setRoleName(domain.getRoleName());
        securityRole.setRoleKey(domain.getRoleKey());
        securityRole.setRoleSort(domain.getRoleSort());
        securityRole.setDataScope(domain.getDataScope());
        securityRole.setStatus(domain.getStatus());
        return securityRole;
    }

    /**
     * 通用安全 SecurityRole → 领域 UserInfoRole
     */
    public static UserInfoRole toDomain(SecurityRole detail) {
        if (detail == null) {
            return null;
        }

        UserInfoRole userInfoRole = new UserInfoRole();
        userInfoRole.setRoleId(detail.getRoleId());
        userInfoRole.setRoleName(detail.getRoleName());
        userInfoRole.setRoleKey(detail.getRoleKey());
        userInfoRole.setRoleSort(detail.getRoleSort());
        userInfoRole.setDataScope(detail.getDataScope());
        userInfoRole.setStatus(detail.getStatus());
        return userInfoRole;
    }

    /**
     * 领域 UserInfoRole 列表 → 通用安全 SecurityRole 列表
     */
    public static List<SecurityRole> toRoleList(List<UserInfoRole> domain) {
        if (domain == null) {
            return null;
        }

        List<SecurityRole> result = new ArrayList<>(domain.size());
        for (UserInfoRole userInfoRole : domain) {
            result.add(toCommon(userInfoRole));
        }
        return result;
    }

    /**
     * 通用安全 SecurityRole 列表 → 领域 UserInfoRole 列表
     */
    public static List<UserInfoRole> toDomainList(List<SecurityRole> common) {
        if (common == null) {
            return null;
        }

        List<UserInfoRole> result = new ArrayList<>(common.size());
        for (SecurityRole securityRole : common) {
            result.add(toDomain(securityRole));
        }
        return result;
    }
}

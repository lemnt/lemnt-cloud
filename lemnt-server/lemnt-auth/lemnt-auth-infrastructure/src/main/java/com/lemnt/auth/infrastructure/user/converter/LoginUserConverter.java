package com.lemnt.auth.infrastructure.user.converter;

import com.lemnt.auth.domain.user.entity.LoginUser;
import com.lemnt.auth.domain.user.entity.UserInfo;
import com.lemnt.auth.domain.user.entity.UserInfoRole;
import com.lemnt.system.api.domain.dto.LoginUserDTO;
import com.lemnt.system.api.domain.dto.UserInfoDTO;
import com.lemnt.system.api.domain.dto.UserInfoRoleDTO;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 登录对象转换器
 *
 * @author wangxx
 * @date 2026-01-06
 */
public class LoginUserConverter {

    /**
     * LoginUserDTO → LoginUser
     *
     * @param item DTO 对象
     * @return 领域实体
     */
    public static LoginUser to(LoginUserDTO item) {
        if (item == null) {
            return null;
        }

        LoginUser loginUser = new LoginUser();

        loginUser.setToken(item.getToken());
        loginUser.setUserId(item.getUserId());
        loginUser.setUsername(item.getUsername());
        loginUser.setLoginTime(item.getLoginTime());
        loginUser.setExpireTime(item.getExpireTime());
        loginUser.setIpaddr(item.getIpaddr());
        loginUser.setRoleKey(item.getRoleKey());

        if (item.getPermissions() != null) {
            loginUser.setPermissions(new LinkedHashSet<>(item.getPermissions()));
        }
        if (item.getRoles() != null) {
            loginUser.setRoles(new LinkedHashSet<>(item.getRoles()));
        }
        loginUser.setDetail(toUserInfo(item.getDetail()));

        return loginUser;
    }

    /**
     * UserInfoDTO → UserInfo
     */
    private static UserInfo toUserInfo(UserInfoDTO dto) {
        if (dto == null) {
            return null;
        }

        UserInfo userInfo = new UserInfo();

        userInfo.setUserId(dto.getUserId());
        userInfo.setDeptId(dto.getDeptId());
        userInfo.setUserName(dto.getUserName());
        userInfo.setNickName(dto.getNickName());
        userInfo.setEmail(dto.getEmail());
        userInfo.setPhonenumber(dto.getPhonenumber());
        userInfo.setSex(dto.getSex());
        userInfo.setAvatar(dto.getAvatar());
        userInfo.setPassword(dto.getPassword());
        userInfo.setStatus(dto.getStatus());
        userInfo.setLoginIp(dto.getLoginIp());
        userInfo.setLoginDate(dto.getLoginDate());
        userInfo.setPwdUpdateDate(dto.getPwdUpdateDate());
        userInfo.setRoles(toUserInfoRoleList(dto.getRoles()));

        return userInfo;
    }

    /**
     * UserInfoRoleDTO → UserInfoRole
     */
    private static UserInfoRole toUserInfoRole(UserInfoRoleDTO dto) {
        if (dto == null) {
            return null;
        }

        UserInfoRole role = new UserInfoRole();

        role.setRoleId(dto.getRoleId());
        role.setRoleName(dto.getRoleName());
        role.setRoleKey(dto.getRoleKey());
        role.setRoleSort(dto.getRoleSort());
        role.setDataScope(dto.getDataScope());
        role.setStatus(dto.getStatus());

        return role;
    }

    /**
     * List&lt;UserInfoRoleDTO&gt; → List&lt;UserInfoRole&gt;
     */
    private static List<UserInfoRole> toUserInfoRoleList(List<UserInfoRoleDTO> list) {
        if (list == null) {
            return null;
        }

        List<UserInfoRole> result = new ArrayList<>(list.size());
        for (UserInfoRoleDTO dto : list) {
            result.add(toUserInfoRole(dto));
        }
        return result;
    }
}


package com.lemnt.system.infrastructure.auth.gateway;

import com.lemnt.system.domain.auth.gateway.UserRoleGateway;
import com.lemnt.system.infrastructure.auth.dataobject.UserRoleDO;
import com.lemnt.system.infrastructure.auth.mapper.UserRoleMapper;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import cn.hutool.core.collection.CollUtil;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/** UserRole 持久化实现。 */
@Component
@RequiredArgsConstructor
public class UserRoleGatewayImpl implements UserRoleGateway {
    private final UserRoleMapper userRoleMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void insertUserRoles(Long userId, List<Long> roleIds) {
        if (CollUtil.isEmpty(roleIds)) {
            return;
        }
        List<UserRoleDO> list = new ArrayList<>();
        for (Long roleId : roleIds) {
            UserRoleDO ur = new UserRoleDO();
            ur.setUserId(userId);
            ur.setRoleId(roleId);
            list.add(ur);
        }
        userRoleMapper.insert(list);
    }

    @Override
    public Boolean deleteUserRoleByUserId(Long userId) {
        return userRoleMapper.deleteUserRoleByUserId(userId) > 0;
    }

    @Override
    public Boolean deleteUserRoleByUserIds(List<Long> userIds) {
        return userRoleMapper.deleteUserRoleByUserIds(userIds) > 0;
    }

    @Override
    public Boolean deleteUserRoleInfo(Long userId, Long roleId) {
        return userRoleMapper.deleteUserRoleInfo(userId, roleId) > 0;
    }

    @Override
    public Boolean deleteUserRoleInfosByRoleId(Long roleId, List<Long> userIds) {
        return userRoleMapper.deleteUserRoleInfos(roleId, userIds) > 0;
    }
}

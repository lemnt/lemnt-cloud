package com.lemnt.system.infrastructure.auth.gateway;

import com.lemnt.system.domain.auth.gateway.UserRoleGateway;
import com.lemnt.system.infrastructure.auth.dataobject.UserRoleDO;
import com.lemnt.system.infrastructure.auth.mapper.UserRoleMapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
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
        return deleteUserRoleByUserIdRow(userId) > 0;
    }

    @Override
    public Boolean deleteUserRoleByUserIds(List<Long> userIds) {
        return deleteUserRoleByUserIdsRow(userIds) > 0;
    }

    @Override
    public Boolean deleteUserRoleInfo(Long userId, Long roleId) {
        return deleteUserRoleInfoRow(userId, roleId) > 0;
    }

    @Override
    public Boolean deleteUserRoleInfosByRoleId(Long roleId, List<Long> userIds) {
        return deleteUserRoleInfosRow(roleId, userIds) > 0;
    }

    /**
     * 通过用户ID删除用户角色关联
     *
     * @param userId 用户ID
     * @return 结果
     */
    private int deleteUserRoleByUserIdRow(Long userId) {
        return userRoleMapper.delete(
                Wrappers.<UserRoleDO>lambdaQuery().eq(UserRoleDO::getUserId, userId));
    }

    /**
     * 批量删除用户角色关联
     *
     * @param userIds 用户ID数组
     * @return 结果
     */
    private int deleteUserRoleByUserIdsRow(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return 0;
        }
        return userRoleMapper.delete(
                Wrappers.<UserRoleDO>lambdaQuery().in(UserRoleDO::getUserId, userIds));
    }

    /**
     * 删除用户和角色关联信息
     *
     * @param roleId 角色ID
     * @param userId 需要删除的用户数据ID
     * @return 结果
     */
    private int deleteUserRoleInfoRow(Long userId, Long roleId) {
        return userRoleMapper.delete(
                Wrappers.<UserRoleDO>lambdaQuery()
                        .eq(UserRoleDO::getRoleId, roleId)
                        .eq(UserRoleDO::getUserId, userId));
    }

    /**
     * 批量取消授权用户角色
     *
     * @param roleId  角色ID
     * @param userIds 需要删除的用户数据ID
     * @return 结果
     */
    private int deleteUserRoleInfosRow(Long roleId, List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return 0;
        }
        return userRoleMapper.delete(
                Wrappers.<UserRoleDO>lambdaQuery()
                        .eq(UserRoleDO::getRoleId, roleId)
                        .in(UserRoleDO::getUserId, userIds));
    }
}

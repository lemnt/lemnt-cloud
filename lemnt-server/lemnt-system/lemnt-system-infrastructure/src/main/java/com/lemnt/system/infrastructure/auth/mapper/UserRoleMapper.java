package com.lemnt.system.infrastructure.auth.mapper;

import com.lemnt.system.infrastructure.auth.dataobject.UserRoleDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * UserRelation 数据层
 *
 * @author wangxx
 */
@Mapper
public interface UserRoleMapper extends BaseMapper<UserRoleDO> {
    /**
     * 通过用户ID删除用户角色关联
     *
     * @param userId 用户ID
     * @return 结果
     */
    default int deleteUserRoleByUserId(Long userId) {
        return this.delete(Wrappers.<UserRoleDO>lambdaQuery()
                .eq(UserRoleDO::getUserId, userId));
    }

    /**
     * 批量删除用户角色关联
     *
     * @param userIds 用户ID数组
     * @return 结果
     */
    default int deleteUserRoleByUserIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return 0;
        }
        return this.delete(Wrappers.<UserRoleDO>lambdaQuery()
                .in(UserRoleDO::getUserId, userIds));
    }

    /**
     * 删除用户和角色关联信息
     *
     * @param roleId 角色ID
     * @param userId 需要删除的用户数据ID
     * @return 结果
     */
    default int deleteUserRoleInfo(Long roleId, Long userId) {
        return this.delete(Wrappers.<UserRoleDO>lambdaQuery()
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
    default int deleteUserRoleInfos(Long roleId, List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return 0;
        }
        return this.delete(Wrappers.<UserRoleDO>lambdaQuery()
                .eq(UserRoleDO::getRoleId, roleId)
                .in(UserRoleDO::getUserId, userIds));
    }
}

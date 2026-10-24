package com.lemnt.system.infrastructure.auth.mapper;

import com.lemnt.system.infrastructure.auth.dataobject.UserPostDO;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * UserRelation 数据层
 *
 * @author wangxx
 */
@Mapper
public interface UserPostMapper extends BaseMapper<UserPostDO> {

    /**
     * 通过岗位ID查询岗位使用数量
     *
     * @param postId 岗位ID
     * @return 结果
     */
    default long countUserPostById(Long postId) {
        return this.selectCount(Wrappers.<UserPostDO>lambdaQuery()
                .eq(UserPostDO::getPostId, postId));
    }

    /**
     * 通过用户ID删除用户岗位关联
     *
     * @param userId 用户ID
     * @return 结果
     */
    default int deleteUserPostByUserId(Long userId) {
        if (userId == null) {
            return 0;
        }
        return this.delete(Wrappers.<UserPostDO>lambdaQuery()
                .eq(UserPostDO::getUserId, userId));
    }

    /**
     * 批量删除用户岗位关联
     *
     * @param userIds 用户ID数组
     * @return 结果
     */
    default int deleteUserPostByUserIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return 0;
        }
        return this.delete(Wrappers.<UserPostDO>lambdaQuery()
                .in(UserPostDO::getUserId, userIds));
    }
}

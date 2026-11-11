package com.lemnt.system.infrastructure.auth.gateway;

import com.lemnt.system.domain.auth.gateway.UserPostGateway;
import com.lemnt.system.infrastructure.auth.dataobject.UserPostDO;
import com.lemnt.system.infrastructure.auth.mapper.UserPostMapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Component;

import cn.hutool.core.collection.CollUtil;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * UserPost 持久化实现。
 */
@Component
@RequiredArgsConstructor
public class UserPostGatewayImpl implements UserPostGateway {
    private final UserPostMapper userPostMapper;

    @Override
    public void insertUserPosts(Long userId, List<Long> postIds) {
        if (CollUtil.isEmpty(postIds)) {
            return;
        }
        List<UserPostDO> list = new ArrayList<>();
        for (Long postId : postIds) {
            UserPostDO up = new UserPostDO();
            up.setUserId(userId);
            up.setPostId(postId);
            list.add(up);
        }
        userPostMapper.insert(list);
    }

    @Override
    public Boolean deleteUserPostByUserId(Long userId) {
        return deleteUserPostByUserIdRow(userId) > 0;
    }

    @Override
    public Boolean deleteUserPostByUserIds(List<Long> userIds) {
        return deleteUserPostByUserIdsRow(userIds) > 0;
    }

    @Override
    public Long countUserPostById(Long postId) {
        return countUserPostByIdRow(postId);
    }

    /**
     * 通过岗位ID查询岗位使用数量
     *
     * @param postId 岗位ID
     * @return 结果
     */
    private long countUserPostByIdRow(Long postId) {
        return userPostMapper.selectCount(
            Wrappers.<UserPostDO>lambdaQuery().eq(UserPostDO::getPostId, postId));
    }

    /**
     * 通过用户ID删除用户岗位关联
     *
     * @param userId 用户ID
     * @return 结果
     */
    private int deleteUserPostByUserIdRow(Long userId) {
        if (userId == null) {
            return 0;
        }
        return userPostMapper.delete(
            Wrappers.<UserPostDO>lambdaQuery().eq(UserPostDO::getUserId, userId));
    }

    /**
     * 批量删除用户岗位关联
     *
     * @param userIds 用户ID数组
     * @return 结果
     */
    private int deleteUserPostByUserIdsRow(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return 0;
        }
        return userPostMapper.delete(
            Wrappers.<UserPostDO>lambdaQuery().in(UserPostDO::getUserId, userIds));
    }
}

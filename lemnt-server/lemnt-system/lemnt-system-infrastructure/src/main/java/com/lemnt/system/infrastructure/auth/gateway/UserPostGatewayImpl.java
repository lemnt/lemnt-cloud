package com.lemnt.system.infrastructure.auth.gateway;

import com.lemnt.system.domain.auth.gateway.UserPostGateway;
import com.lemnt.system.infrastructure.auth.dataobject.UserPostDO;
import com.lemnt.system.infrastructure.auth.mapper.UserPostMapper;

import org.springframework.stereotype.Component;

import cn.hutool.core.collection.CollUtil;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/** UserPost 持久化实现。 */
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
        return userPostMapper.deleteUserPostByUserId(userId) > 0;
    }

    @Override
    public Boolean deleteUserPostByUserIds(List<Long> userIds) {
        return userPostMapper.deleteUserPostByUserIds(userIds) > 0;
    }
    @Override
    public Long countUserPostById(Long postId) {
        return userPostMapper.countUserPostById(postId);
    }
}

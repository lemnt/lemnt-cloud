package com.lemnt.system.infrastructure.auth.gateway;

import java.util.ArrayList;
import java.util.List;

import cn.hutool.core.collection.CollUtil;
import com.lemnt.common.domain.model.PageQry;
import com.lemnt.common.domain.model.PageResult;
import com.lemnt.system.infrastructure.auth.mapper.UserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import com.lemnt.common.core.constant.CacheConstants;
import com.lemnt.common.redis.service.RedisStringService;
import com.lemnt.system.domain.auth.model.User;
import com.lemnt.system.domain.auth.gateway.UserGateway;
import com.lemnt.system.infrastructure.auth.converter.UserConverter;
import com.lemnt.system.infrastructure.auth.dataobject.UserDO;
import com.lemnt.system.infrastructure.auth.dataobject.UserPostDO;
import com.lemnt.system.infrastructure.auth.dataobject.UserRoleDO;
import com.lemnt.system.infrastructure.auth.mapper.UserMapper;
import com.lemnt.system.infrastructure.auth.mapper.UserPostMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户管理 Gateway 实现
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class UserGatewayImpl implements UserGateway {

    private final UserMapper userMapper;
    private final UserPostMapper userPostMapper;
    private final UserRoleMapper userRoleMapper;
    private final RedisStringService redisStringService;

    @Override
    public PageResult<User> selectPage(User user, PageQry pageQry) {
        Page<User> page = new Page<>(pageQry.getPageNum(), pageQry.getPageSize());
        IPage<User> result = userMapper.selectPage(page, user);
        return PageResult.of(result.getRecords(), result.getTotal());
    }

    @Override
    public PageResult<User> selectAllocatedList(User user, PageQry pageQry) {
        Page<UserDO> page = new Page<>(pageQry.getPageNum(), pageQry.getPageSize());
        IPage<UserDO> result = userMapper.selectAllocatedList(page, user);
        return PageResult.of(UserConverter.toEntityList(result.getRecords()), result.getTotal());
    }

    @Override
    public PageResult<User> selectUnallocatedList(User user, PageQry pageQry) {
        Page<UserDO> page = new Page<>(pageQry.getPageNum(), pageQry.getPageSize());
        IPage<UserDO> result = userMapper.selectUnallocatedList(page, user);
        return PageResult.of(UserConverter.toEntityList(result.getRecords()), result.getTotal());
    }

    @Override
    public User selectUserByUserName(String userName) {
        return userMapper.selectUserByUserName(userName);
    }

    @Override
    public User selectUserById(Long userId) {
        return userMapper.selectUserById(userId);
    }

    @Override
    public Boolean insertUser(User user) {
        UserDO userDO = UserConverter.toDO(user);
        int rows = userMapper.insert(userDO);
        // 回填自增主键到 Entity，供调用方获取 userId
        if (rows > 0) {
            user.setUserId(userDO.getUserId());
        }
        return rows > 0;
    }

    @Override
    public Boolean updateUser(User user) {
        return userMapper.updateById(UserConverter.toDO(user)) > 0;
    }

    @Override
    public Boolean updateUserAvatar(Long userId, String avatar) {
        return userMapper.updateUserAvatar(userId, avatar) > 0;
    }

    @Override
    public Boolean updateUserStatus(User user) {
        return userMapper.updateUserStatus(user.getUserId(), user.getStatus()) > 0;
    }

    @Override
    public Boolean updateLoginInfo(User user) {
        return userMapper.updateLoginInfo(UserConverter.toDO(user)) > 0;
    }

    @Override
    public Boolean resetUserPwd(Long userId, String password) {
        return userMapper.resetUserPwd(userId, password) > 0;
    }

    @Override
    public Boolean deleteUserById(Long userId) {
        return userMapper.deleteById(userId) > 0;
    }

    @Override
    public Boolean deleteUserByIds(List<Long> userIds) {
        return userMapper.deleteByIds(userIds) > 0;
    }

    @Override
    public User checkUserNameUnique(String userName) {
        return UserConverter.toEntity(userMapper.checkUserNameUnique(userName));
    }

    @Override
    public User checkPhoneUnique(String phonenumber) {
        return UserConverter.toEntity(userMapper.checkPhoneUnique(phonenumber));
    }

    @Override
    public User checkEmailUnique(String email) {
        return UserConverter.toEntity(userMapper.checkEmailUnique(email));
    }

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
    public Boolean deleteUserRoleByUserId(Long userId) {
        return userRoleMapper.deleteUserRoleByUserId(userId) > 0;
    }

    @Override
    public Boolean deleteUserPostByUserId(Long userId) {
        return userPostMapper.deleteUserPostByUserId(userId) > 0;
    }

    @Override
    public Boolean deleteUserRoleByUserIds(List<Long> userIds) {
        return userRoleMapper.deleteUserRoleByUserIds(userIds) > 0;
    }

    @Override
    public Boolean deleteUserPostByUserIds(List<Long> userIds) {
        return userPostMapper.deleteUserPostByUserIds(userIds) > 0;
    }

    @Override
    public Boolean deleteUserRoleInfo(Long userId, Long roleId) {
        return userRoleMapper.deleteUserRoleInfo(userId, roleId) > 0;
    }

    @Override
    public Boolean deleteUserRoleInfosByRoleId(Long roleId, List<Long> userIds) {
        return userRoleMapper.deleteUserRoleInfos(roleId, userIds) > 0;
    }

    @Override
    public void unlockUser(String userName) {
        String cacheKey = CacheConstants.PWD_ERR_CNT_KEY + userName;
        redisStringService.delete(cacheKey);
    }
}

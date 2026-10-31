package com.lemnt.system.infrastructure.auth.gateway;

import com.lemnt.common.domain.model.PageQry;
import com.lemnt.common.domain.model.PageResult;
import com.lemnt.system.domain.auth.gateway.UserGateway;
import com.lemnt.system.domain.auth.model.User;
import com.lemnt.system.infrastructure.auth.converter.UserConverter;
import com.lemnt.system.infrastructure.auth.dataobject.UserDO;
import com.lemnt.system.infrastructure.auth.mapper.UserMapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * 用户管理 Gateway 实现
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class UserGatewayImpl implements UserGateway {
    private final UserMapper userMapper;

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
    public Boolean checkDeptExistUser(Long deptId) {
        return userMapper.checkDeptExistUser(deptId) > 0;
    }
}

package com.lemnt.system.infrastructure.auth.gateway;

import com.lemnt.common.domain.model.PageQry;
import com.lemnt.common.domain.model.PageResult;
import com.lemnt.system.domain.auth.gateway.UserGateway;
import com.lemnt.system.domain.auth.model.User;
import com.lemnt.system.infrastructure.auth.converter.UserConverter;
import com.lemnt.system.infrastructure.auth.dataobject.UserDO;
import com.lemnt.system.infrastructure.auth.mapper.UserMapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
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
        return updateUserAvatarRow(userId, avatar) > 0;
    }

    @Override
    public Boolean updateUserStatus(User user) {
        return updateUserStatusRow(user.getUserId(), user.getStatus()) > 0;
    }

    @Override
    public Boolean updateLoginInfo(User user) {
        return updateLoginInfoRow(UserConverter.toDO(user)) > 0;
    }

    @Override
    public Boolean resetUserPwd(Long userId, String password) {
        return resetUserPwdRow(userId, password) > 0;
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
        return UserConverter.toEntity(checkUserNameUniqueRow(userName));
    }

    @Override
    public User checkPhoneUnique(String phonenumber) {
        return UserConverter.toEntity(checkPhoneUniqueRow(phonenumber));
    }

    @Override
    public User checkEmailUnique(String email) {
        return UserConverter.toEntity(checkEmailUniqueRow(email));
    }

    @Override
    public Boolean checkDeptExistUser(Long deptId) {
        return checkDeptExistUserRow(deptId) > 0;
    }

    /**
     * 修改用户头像
     *
     * @param userId 用户ID
     * @param avatar 头像路径
     * @return 结果
     */
    private int updateUserAvatarRow(Long userId, String avatar) {
        return userMapper.update(
                Wrappers.lambdaUpdate(UserDO.class)
                        .eq(UserDO::getUserId, userId)
                        .set(UserDO::getAvatar, avatar));
    }

    /**
     * 修改用户状态
     *
     * @param userId 用户ID
     * @param status 状态
     * @return 结果
     */
    private int updateUserStatusRow(Long userId, String status) {
        return userMapper.update(
                Wrappers.lambdaUpdate(UserDO.class)
                        .eq(UserDO::getUserId, userId)
                        .set(UserDO::getStatus, status));
    }

    /**
     * 更新用户登录信息
     *
     * @param user 用户信息
     * @return 结果
     */
    private int updateLoginInfoRow(UserDO user) {
        return userMapper.update(
                Wrappers.lambdaUpdate(UserDO.class)
                        .eq(UserDO::getUserId, user.getUserId())
                        .set(UserDO::getLoginDate, user.getLoginDate())
                        .set(UserDO::getLoginIp, user.getLoginIp()));
    }

    /**
     * 重置用户密码
     *
     * @param userId   用户ID
     * @param password 新密码
     * @return 结果
     */
    private int resetUserPwdRow(Long userId, String password) {
        return userMapper.update(
                Wrappers.lambdaUpdate(UserDO.class)
                        .eq(UserDO::getUserId, userId)
                        .set(UserDO::getPassword, password)
                        .set(UserDO::getPwdUpdateDate, LocalDateTime.now()));
    }

    /**
     * 校验用户名是否唯一
     *
     * @param userName 用户名
     * @return 用户信息
     */
    private UserDO checkUserNameUniqueRow(String userName) {
        return userMapper.selectOne(
                Wrappers.<UserDO>lambdaQuery().eq(UserDO::getUserName, userName).last("limit 1"));
    }

    /**
     * 校验手机号是否唯一
     *
     * @param phonenumber 手机号
     * @return 用户信息
     */
    private UserDO checkPhoneUniqueRow(String phonenumber) {
        return userMapper.selectOne(
                Wrappers.<UserDO>lambdaQuery()
                        .eq(UserDO::getPhonenumber, phonenumber)
                        .last("limit 1"));
    }

    /**
     * 校验邮箱是否唯一
     *
     * @param email 邮箱
     * @return 用户信息
     */
    private UserDO checkEmailUniqueRow(String email) {
        return userMapper.selectOne(
                Wrappers.<UserDO>lambdaQuery().eq(UserDO::getEmail, email).last("limit 1"));
    }

    /**
     * 查询部门是否存在用户
     *
     * @param deptId 部门ID
     * @return 结果
     */
    private long checkDeptExistUserRow(Long deptId) {
        return userMapper.selectCount(Wrappers.<UserDO>lambdaQuery().eq(UserDO::getDeptId, deptId));
    }
}

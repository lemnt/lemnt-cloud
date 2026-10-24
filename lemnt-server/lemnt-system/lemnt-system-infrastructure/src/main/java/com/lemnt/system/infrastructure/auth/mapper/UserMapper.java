package com.lemnt.system.infrastructure.auth.mapper;

import com.lemnt.common.datascope.annotation.DataScope;
import com.lemnt.system.domain.auth.model.User;
import com.lemnt.system.infrastructure.auth.dataobject.UserDO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * User 数据层
 *
 * @author wangxx
 */
@Mapper
public interface UserMapper extends BaseMapper<UserDO> {

    /**
     * 根据条件分页查询用户列表
     *
     * @param page 分页参数
     * @param user 用户信息
     * @return 分页结果
     */
    @DataScope(deptAlias = "d", userAlias = "u")
    IPage<User> selectPage(Page<User> page, @Param("qry") User user);

    /**
     * 根据条件查询已配用户角色列表
     *
     * @param user 用户信息
     * @return 用户列表
     */
    @DataScope(deptAlias = "d", userAlias = "u")
    IPage<UserDO> selectAllocatedList(Page<UserDO> page, @Param("qry") User user);

    /**
     * 根据条件查询未分配用户角色列表
     *
     * @param user 用户信息
     * @return 用户列表
     */
    @DataScope(deptAlias = "d", userAlias = "u")
    IPage<UserDO> selectUnallocatedList(Page<UserDO> page, @Param("qry") User user);

    /**
     * 通过用户名查询用户
     *
     * @param userName 用户名
     * @return 用户信息
     */
    User selectUserByUserName(String userName);

    /**
     * 通过用户ID查询用户
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    User selectUserById(Long userId);

    /**
     * 修改用户头像
     *
     * @param userId 用户ID
     * @param avatar 头像路径
     * @return 结果
     */
    default int updateUserAvatar(Long userId, String avatar) {
        return this.update(Wrappers.lambdaUpdate(UserDO.class)
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
    default int updateUserStatus(Long userId, String status) {
        return this.update(Wrappers.lambdaUpdate(UserDO.class)
                .eq(UserDO::getUserId, userId)
                .set(UserDO::getStatus, status));
    }

    /**
     * 更新用户登录信息
     *
     * @param user 用户信息
     * @return 结果
     */
    default int updateLoginInfo(UserDO user) {
        return this.update(Wrappers.lambdaUpdate(UserDO.class)
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
    default int resetUserPwd(Long userId, String password) {
        return this.update(Wrappers.lambdaUpdate(UserDO.class)
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
    default UserDO checkUserNameUnique(String userName) {
        return this.selectOne(Wrappers.<UserDO>lambdaQuery()
                .eq(UserDO::getUserName, userName)
                .last("limit 1"));
    }

    /**
     * 校验手机号是否唯一
     *
     * @param phonenumber 手机号
     * @return 用户信息
     */
    default UserDO checkPhoneUnique(String phonenumber) {
        return this.selectOne(Wrappers.<UserDO>lambdaQuery()
                .eq(UserDO::getPhonenumber, phonenumber)
                .last("limit 1"));
    }

    /**
     * 校验邮箱是否唯一
     *
     * @param email 邮箱
     * @return 用户信息
     */
    default UserDO checkEmailUnique(String email) {
        return this.selectOne(Wrappers.<UserDO>lambdaQuery()
                .eq(UserDO::getEmail, email)
                .last("limit 1"));
    }

    /**
     * 查询部门是否存在用户
     *
     * @param deptId 部门ID
     * @return 结果
     */
    default long checkDeptExistUser(Long deptId) {
        return this.selectCount(Wrappers.<UserDO>lambdaQuery().eq(UserDO::getDeptId, deptId));
    }
}

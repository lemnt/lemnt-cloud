package com.lemnt.system.domain.auth.gateway;

import com.lemnt.common.domain.model.PageQry;
import com.lemnt.common.domain.model.PageResult;
import com.lemnt.system.domain.auth.model.User;

import java.util.List;

/**
 * 用户管理 Gateway 端口
 *
 * @author wangxx
 */
public interface UserGateway {
    /**
     * 根据条件分页查询用户列表
     *
     * @param user    用户信息
     * @param pageQry 分页参数
     * @return 分页结果
     */
    PageResult<User> selectPage(User user, PageQry pageQry);

    /**
     * 根据条件查询已配用户角色列表
     *
     * @param user 用户信息
     * @return 用户列表
     */
    PageResult<User> selectAllocatedList(User user, PageQry pageQry);

    /**
     * 根据条件查询未分配用户角色列表
     *
     * @param user 用户信息
     * @return 用户列表
     */
    PageResult<User> selectUnallocatedList(User user, PageQry pageQry);

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
     * 新增用户
     *
     * @param user 用户信息
     * @return 是否成功
     */
    Boolean insertUser(User user);

    /**
     * 修改用户
     *
     * @param user 用户信息
     * @return 是否成功
     */
    Boolean updateUser(User user);

    /**
     * 修改用户头像
     *
     * @param userId 用户ID
     * @param avatar 头像路径
     * @return 是否成功
     */
    Boolean updateUserAvatar(Long userId, String avatar);

    /**
     * 修改用户状态
     *
     * @param user 用户
     * @return 是否成功
     */
    Boolean updateUserStatus(User user);

    /**
     * 更新用户登录信息
     *
     * @param user 用户信息（含loginIp/loginDate）
     * @return 是否成功
     */
    Boolean updateLoginInfo(User user);

    /**
     * 重置用户密码
     *
     * @param userId   用户ID
     * @param password 新密码
     * @return 结果
     */
    Boolean resetUserPwd(Long userId, String password);

    /**
     * 通过用户ID删除用户
     *
     * @param userId 用户ID
     * @return 是否成功
     */
    Boolean deleteUserById(Long userId);

    /**
     * 批量删除用户
     *
     * @param userIds 用户ID组
     * @return 是否成功
     */
    Boolean deleteUserByIds(List<Long> userIds);

    /**
     * 校验用户名是否唯一
     *
     * @param userName 用户名
     * @return 用户信息
     */
    User checkUserNameUnique(String userName);

    /**
     * 校验手机号是否唯一
     *
     * @param phonenumber 手机号
     * @return 用户信息
     */
    User checkPhoneUnique(String phonenumber);

    /**
     * 校验邮箱是否唯一
     *
     * @param email 邮箱
     * @return 用户信息
     */
    User checkEmailUnique(String email);

    /**
     * 查询部门是否存在用户
     *
     * @param deptId 部门ID
     * @return 结果
     */
    Boolean checkDeptExistUser(Long deptId);
}

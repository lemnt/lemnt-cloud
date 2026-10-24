package com.lemnt.system.domain.auth.gateway;

import java.util.List;

import com.lemnt.common.domain.model.PageQry;
import com.lemnt.common.domain.model.PageResult;
import com.lemnt.system.domain.auth.model.User;

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
     * 新增用户角色关联
     *
     * @param userId  用户ID
     * @param roleIds 角色ID数组
     */
    void insertUserRoles(Long userId, List<Long> roleIds);

    /**
     * 新增用户岗位关联
     *
     * @param userId  用户ID
     * @param postIds 岗位ID数组
     */
    void insertUserPosts(Long userId, List<Long> postIds);

    /**
     * 删除用户角色关联
     *
     * @param userId 用户ID
     * @return 是否成功
     */
    Boolean deleteUserRoleByUserId(Long userId);

    /**
     * 删除用户岗位关联
     *
     * @param userId 用户ID
     * @return 是否成功
     */
    Boolean deleteUserPostByUserId(Long userId);

    /**
     * 删除单个用户角色关联
     *
     * @param userId 用户ID
     * @param roleId 角色ID
     * @return 是否成功
     */
    Boolean deleteUserRoleInfo(Long userId, Long roleId);

    /**
     * 批量取消授权（同一角色下多个用户）
     *
     * @param roleId  角色ID
     * @param userIds 用户ID数组
     * @return 是否成功
     */
    Boolean deleteUserRoleInfosByRoleId(Long roleId, List<Long> userIds);

    /**
     * 批量删除用户角色关联
     *
     * @param userIds 用户ID数组
     * @return 是否成功
     */
    Boolean deleteUserRoleByUserIds(List<Long> userIds);

    /**
     * 批量删除用户岗位关联
     *
     * @param userIds 用户ID数组
     * @return 是否成功
     */
    Boolean deleteUserPostByUserIds(List<Long> userIds);

    /**
     * 解锁用户（清除登录失败次数缓存）
     *
     * @param userName 用户名
     */
    void unlockUser(String userName);
}

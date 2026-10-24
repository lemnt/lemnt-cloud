package com.lemnt.system.api;

import com.lemnt.system.api.domain.bo.UserBo;
import com.lemnt.system.api.domain.dto.LoginUserDTO;

/**
 * 用户服务
 *
 * @author wangxx
 */
public interface RemoteUserService {
    /**
     * 通过用户名查询用户信息
     *
     * @param username 用户名
     * @return 结果
     */
    LoginUserDTO getUserInfo(String username);

    /**
     * 后台创建用户并返回用户ID。
     */
    Long createUser(UserBo bo);

    /**
     * 删除用户，用于跨服务创建失败时补偿。
     */
    Boolean deleteUser(Long userId);

    /**
     * 修改用户状态。
     *
     * @param userIds 用户ID列表
     * @param status  用户状态，1正常、2停用
     * @return 是否成功
     */
    Boolean updateUserStatus(java.util.List<Long> userIds, String status);

    /**
     * 记录用户登录IP地址和登录时间
     *
     * @param bo 用户信息
     * @return 结果
     */
    Boolean recordUserLogin(UserBo bo);

}

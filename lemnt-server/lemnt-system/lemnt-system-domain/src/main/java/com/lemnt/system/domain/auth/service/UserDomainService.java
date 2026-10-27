package com.lemnt.system.domain.auth.service;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.common.core.utils.StringUtils;
import com.lemnt.system.domain.auth.gateway.UserGateway;
import com.lemnt.system.domain.auth.model.User;

import lombok.RequiredArgsConstructor;

/**
 * 用户领域服务 — 封装跨 Entity 或需要 Gateway 辅助的业务规则
 *
 * @author wangxx
 */
@RequiredArgsConstructor
public class UserDomainService {
    private final UserGateway userGateway;

    /**
     * 校验用户登录名是否唯一
     *
     * @param userName 登录名
     * @throws ServiceException 已存在时抛出
     */
    public void checkUserNameUnique(String userName) {
        if (userGateway.checkUserNameUnique(userName) != null) {
            throw new ServiceException("登录账号'" + userName + "'已存在");
        }
    }

    /**
     * 校验手机号是否唯一（新增场景，不排除自身）
     *
     * @param phonenumber 手机号
     * @throws ServiceException 已存在时抛出
     */
    public void checkPhoneUnique(String phonenumber) {
        if (StringUtils.isNotEmpty(phonenumber) && userGateway.checkPhoneUnique(phonenumber) != null) {
            throw new ServiceException("手机号码'" + phonenumber + "'已存在");
        }
    }

    /**
     * 校验手机号是否唯一（修改场景，排除自身 userId）
     *
     * @param userId      当前用户 ID
     * @param phonenumber 手机号
     * @throws ServiceException 已存在时抛出
     */
    public void checkPhoneUnique(Long userId, String phonenumber) {
        if (StringUtils.isNotEmpty(phonenumber)) {
            User exist = userGateway.checkPhoneUnique(phonenumber);
            if (exist != null && !exist.getUserId().equals(userId)) {
                throw new ServiceException("手机号码'" + phonenumber + "'已存在");
            }
        }
    }

    /**
     * 校验邮箱是否唯一（新增场景，不排除自身）
     *
     * @param email 邮箱
     * @throws ServiceException 已存在时抛出
     */
    public void checkEmailUnique(String email) {
        if (StringUtils.isNotEmpty(email) && userGateway.checkEmailUnique(email) != null) {
            throw new ServiceException("邮箱账号'" + email + "'已存在");
        }
    }

    /**
     * 校验邮箱是否唯一（修改场景，排除自身 userId）
     *
     * @param userId 当前用户 ID
     * @param email  邮箱
     * @throws ServiceException 已存在时抛出
     */
    public void checkEmailUnique(Long userId, String email) {
        if (StringUtils.isNotEmpty(email)) {
            User exist = userGateway.checkEmailUnique(email);
            if (exist != null && !exist.getUserId().equals(userId)) {
                throw new ServiceException("邮箱账号'" + email + "'已存在");
            }
        }
    }

    /**
     * 根据 userId 查询用户，不存在则抛出业务异常
     *
     * @param userId 用户 ID
     * @return 用户实体
     */
    public void requireById(Long userId) {
        User user = userGateway.selectUserById(userId);
        if (user == null) {
            throw new ServiceException("用户不存在，ID=" + userId);
        }
    }
}

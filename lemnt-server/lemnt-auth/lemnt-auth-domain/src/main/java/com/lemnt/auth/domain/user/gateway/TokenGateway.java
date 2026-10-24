package com.lemnt.auth.domain.user.gateway;

import com.lemnt.auth.domain.user.entity.LoginUser;
import com.lemnt.auth.domain.user.entity.TokenResult;

/**
 * Token 领域网关
 * <p>
 * 封装 Token 的创建、刷新、查询与注销操作，将 JWT + Redis 实现细节屏蔽在基础设施层。
 * </p>
 *
 * @author wangxx
 */
public interface TokenGateway {

    /**
     * 创建令牌
     *
     * @param loginUser 登录用户（领域实体）
     * @return token 创建结果
     */
    TokenResult createToken(LoginUser loginUser);

    /**
     * 刷新令牌有效期
     *
     * @param loginUser 登录用户（领域实体）
     */
    void refreshToken(LoginUser loginUser);

    /**
     * 根据 token 获取登录用户信息
     *
     * @param token 令牌
     * @return 登录用户（领域实体），token 无效时返回 null
     */
    LoginUser getLoginUser(String token);

    /**
     * 根据 token 注销登录（清除缓存）
     *
     * @param token 令牌
     */
    void logoutByToken(String token);
}
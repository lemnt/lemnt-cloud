package com.lemnt.auth.domain.user.gateway;

import com.lemnt.auth.domain.user.entity.LoginUser;

/**
 * 记录日志方法
 *
 * @author wangxx
 */
public interface UserGateway {

    /**
     * 记录登录信息
     *
     * @param username 用户名
     * @return
     */
    LoginUser getUserInfo(String username);

}

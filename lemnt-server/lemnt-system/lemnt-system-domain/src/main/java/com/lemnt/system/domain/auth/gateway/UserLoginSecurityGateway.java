package com.lemnt.system.domain.auth.gateway;

/** UserLoginSecurity 存储接口。 */
public interface UserLoginSecurityGateway {
    /**
     * 解锁用户（清除登录失败次数缓存）
     *
     * @param userName 用户名
     */
    void unlockUser(String userName);
}

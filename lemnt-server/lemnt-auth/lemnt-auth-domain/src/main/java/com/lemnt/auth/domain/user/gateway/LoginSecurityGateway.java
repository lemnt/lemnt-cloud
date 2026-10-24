package com.lemnt.auth.domain.user.gateway;

/**
 * 登录安全领域网关
 * <p>
 * 封装密码重试锁定与 IP 黑名单等登录安全策略，将 Redis 实现细节屏蔽在基础设施层。
 * </p>
 *
 * @author wangxx
 */
public interface LoginSecurityGateway {

    /**
     * 获取当前重试次数
     *
     * @param username 用户名
     * @return 已重试次数
     */
    int getRetryCount(String username);

    /**
     * 重试次数 +1，并更新锁定过期时间
     *
     * @param username 用户名
     */
    void incrementRetryCount(String username);

    /**
     * 登录成功后清除重试记录
     *
     * @param username 用户名
     */
    void clearRetryCount(String username);

    /**
     * 账户是否已被锁定（重试次数达到上限）
     *
     * @param username 用户名
     * @return true 已锁定
     */
    boolean isLocked(String username);

    /**
     * 获取登录 IP 黑名单列表
     *
     * @return 逗号分隔的 IP 黑名单字符串，无可返回 null
     */
    String getBlackIpList();
}
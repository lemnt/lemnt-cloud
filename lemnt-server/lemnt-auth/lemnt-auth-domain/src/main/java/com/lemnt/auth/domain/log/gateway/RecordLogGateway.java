package com.lemnt.auth.domain.log.gateway;

/**
 * 记录日志方法
 *
 * @author wangxx
 */
public interface RecordLogGateway {

    /**
     * 记录登录信息
     *
     * @param username 用户名
     * @param status   状态
     * @param message  消息内容
     * @return
     */
    void recordLogininfor(String username, Integer loginType, String status, String message);

    /**
     * 记录登录信息
     *
     * @param userId 用户ID
     */
    void recordLoginInfo(Long userId);
}

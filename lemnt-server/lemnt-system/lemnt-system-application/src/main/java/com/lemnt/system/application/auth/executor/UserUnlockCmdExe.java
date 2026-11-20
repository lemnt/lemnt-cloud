package com.lemnt.system.application.auth.executor;

import com.lemnt.system.domain.auth.gateway.UserLoginSecurityGateway;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 解锁用户命令执行器（清除登录失败次数缓存）
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class UserUnlockCmdExe {
    private final UserLoginSecurityGateway userLoginSecurityGateway;

    /**
     * 解锁用户
     *
     * @param userName 用户名
     * @return 结果
     */
    public Boolean execute(String userName) {
        userLoginSecurityGateway.unlockUser(userName);
        return true;
    }
}

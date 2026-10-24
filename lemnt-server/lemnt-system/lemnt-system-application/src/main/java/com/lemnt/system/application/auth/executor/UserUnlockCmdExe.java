package com.lemnt.system.application.auth.executor;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import com.lemnt.system.domain.auth.gateway.UserGateway;

/**
 * 解锁用户命令执行器（清除登录失败次数缓存）
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class UserUnlockCmdExe {

    private final UserGateway userGateway;

    /**
     * 解锁用户
     *
     * @param userName 用户名
     * @return 结果
     */
    public Boolean execute(String userName) {
        userGateway.unlockUser(userName);
        return true;
    }
}

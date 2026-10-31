package com.lemnt.system.application.auth.executor;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.common.security.utils.SecurityUtils;
import com.lemnt.system.domain.auth.gateway.UserGateway;
import com.lemnt.system.domain.auth.model.User;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 当前登录用户头像更新执行器。
 */
@Component
@RequiredArgsConstructor
public class UserAvatarUpdateCmdExe {
    private final UserGateway userGateway;

    /**
     * 更新头像并返回旧头像地址。
     */
    public String execute(String avatar) {
        Long userId = SecurityUtils.getUserId();
        User user = userGateway.selectUserById(userId);
        if (!Boolean.TRUE.equals(userGateway.updateUserAvatar(userId, avatar))) {
            throw new ServiceException("更新用户头像失败");
        }
        return user == null ? null : user.getAvatar();
    }
}

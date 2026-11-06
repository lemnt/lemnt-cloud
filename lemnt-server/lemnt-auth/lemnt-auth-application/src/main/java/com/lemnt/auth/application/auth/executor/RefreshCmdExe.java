package com.lemnt.auth.application.auth.executor;

import com.lemnt.auth.domain.user.entity.LoginUser;
import com.lemnt.auth.domain.user.gateway.TokenGateway;
import com.lemnt.common.core.utils.StringUtils;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * 刷新令牌有效期命令处理器
 *
 * @author wangxx
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RefreshCmdExe {
    private final TokenGateway tokenGateway;

    /**
     * 处理刷新令牌命令
     *
     * @param token 令牌
     * @return 是否成功
     */
    public Boolean execute(String token) {
        if (StringUtils.isNotBlank(token)) {
            LoginUser loginUser = tokenGateway.getLoginUser(token);
            if (Objects.nonNull(loginUser)) {
                // 刷新令牌有效期
                tokenGateway.refreshToken(loginUser);
            }
        }
        return Boolean.TRUE;
    }
}

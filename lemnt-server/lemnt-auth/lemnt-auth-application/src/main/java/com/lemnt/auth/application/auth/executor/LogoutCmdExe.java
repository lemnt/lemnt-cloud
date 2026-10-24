package com.lemnt.auth.application.auth.executor;

import com.lemnt.auth.domain.log.enums.LoginStatusEnum;
import com.lemnt.auth.domain.log.enums.LoginTypeEnum;
import com.lemnt.auth.domain.log.gateway.RecordLogGateway;
import com.lemnt.auth.domain.user.gateway.TokenGateway;
import com.lemnt.auth.domain.user.entity.LoginUser;
import com.lemnt.common.core.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 用户退出登录命令处理器
 *
 * @author wangxx
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LogoutCmdExe {

    private final RecordLogGateway recordLogGateway;
    private final TokenGateway tokenGateway;

    /**
     * 处理退出登录命令
     *
     * @param token 令牌
     * @return 是否成功
     */
    public Boolean execute(String token) {
        if (StringUtils.isNotBlank(token)) {
            // 获取用户信息用于日志记录
            LoginUser loginUser = tokenGateway.getLoginUser(token);
            // 删除用户缓存记录
            tokenGateway.logoutByToken(token);
            // 记录用户退出日志
            String username = loginUser != null ? loginUser.getUsername() : "unknown";
            recordLogGateway.recordLogininfor(username, LoginTypeEnum.LOGIN_OUT.getCode(), LoginStatusEnum.SUCCESS.getCode(), "退出成功");
        }
        return Boolean.TRUE;
    }
}
package com.lemnt.auth.application.auth.executor;

import com.lemnt.auth.application.auth.cmd.LoginCmd;
import com.lemnt.auth.domain.log.enums.LoginStatusEnum;
import com.lemnt.auth.domain.log.enums.LoginTypeEnum;
import com.lemnt.auth.domain.log.gateway.RecordLogGateway;
import com.lemnt.auth.domain.user.entity.LoginUser;
import com.lemnt.auth.domain.user.entity.TokenResult;
import com.lemnt.auth.domain.user.entity.UserInfo;
import com.lemnt.auth.domain.user.gateway.LoginSecurityGateway;
import com.lemnt.auth.domain.user.gateway.PasswordEncoderGateway;
import com.lemnt.auth.domain.user.gateway.TokenGateway;
import com.lemnt.auth.domain.user.gateway.UserGateway;
import com.lemnt.common.core.constant.CacheConstants;
import com.lemnt.common.core.constant.UserConstants;
import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.common.core.utils.ip.IpUtils;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoginCmdExe {
    private final UserGateway userGateway;
    private final RecordLogGateway recordLogGateway;
    private final LoginSecurityGateway loginSecurityGateway;
    private final TokenGateway tokenGateway;
    private final PasswordEncoderGateway passwordEncoderGateway;
    public TokenResult execute(LoginCmd cmd) {
        String username = cmd.username();
        String password = cmd.password();
        if (StringUtils.isAnyBlank(cmd.username(), cmd.password())) {
            recordLogGateway.recordLogininfor(username,
                    LoginTypeEnum.LOGIN.getCode(),
                    LoginStatusEnum.FAIL.getCode(),
            "用户/密码必须填写");
            throw new ServiceException("用户/密码必须填写");
        }
        if (password.length() < UserConstants.PASSWORD_MIN_LENGTH
                || password.length() > UserConstants.PASSWORD_MAX_LENGTH) {
            recordLogGateway.recordLogininfor(username,
                    LoginTypeEnum.LOGIN.getCode(),
                    LoginStatusEnum.FAIL.getCode(),
            "用户密码不在指定范围");
            throw new ServiceException("用户密码不在指定范围");
        }
        if (username.length() < UserConstants.USERNAME_MIN_LENGTH
                || username.length() > UserConstants.USERNAME_MAX_LENGTH) {
            recordLogGateway.recordLogininfor(username,
                    LoginTypeEnum.LOGIN.getCode(),
                    LoginStatusEnum.FAIL.getCode(),
            "用户名不在指定范围");
            throw new ServiceException("用户名不在指定范围");
        }
        String blackStr = loginSecurityGateway.getBlackIpList();
        if (IpUtils.isMatchedIp(blackStr, IpUtils.getIpAddr())) {
            recordLogGateway.recordLogininfor(username,
                    LoginTypeEnum.LOGIN.getCode(),
                    LoginStatusEnum.FAIL.getCode(),
            "很遗憾，访问IP已被列入系统黑名单");
            throw new ServiceException("很遗憾，访问IP已被列入系统黑名单");
        }
        LoginUser userInfo = userGateway.getUserInfo(username);
        if (Objects.isNull(userInfo)) {
            throw new ServiceException("对不起，您的账号：" + username + " 不存在");
        }
        UserInfo user = userInfo.getDetail();
        if (user.isDisabled()) {
            recordLogGateway.recordLogininfor(username,
                    LoginTypeEnum.LOGIN.getCode(),
                    LoginStatusEnum.FAIL.getCode(),
            "用户已停用，请联系管理员");
            throw new ServiceException("对不起，您的账号：" + username + " 已停用");
        }
        validate(user, password);
        recordLogGateway.recordLogininfor(username,
                LoginTypeEnum.LOGIN.getCode(),
                LoginStatusEnum.SUCCESS.getCode(),
        "登录成功");
        recordLogGateway.recordLoginInfo(user.getUserId());
        return tokenGateway.createToken(userInfo);
    }

    private void validate(UserInfo user, String password) {
        String username = user.getUserName();
        if (loginSecurityGateway.isLocked(username)) {
            String errMsg = String.format("密码输入错误%s次，帐户锁定%s分钟",
                    CacheConstants.PASSWORD_MAX_RETRY_COUNT,
                    CacheConstants.PASSWORD_LOCK_TIME);
            recordLogGateway.recordLogininfor(username,
                    LoginTypeEnum.LOGIN.getCode(),
                    LoginStatusEnum.FAIL.getCode(),
                    errMsg);
            throw new ServiceException(errMsg);
        }
        if (!user.verifyPassword(password, passwordEncoderGateway)) {
            loginSecurityGateway.incrementRetryCount(username);
            recordLogGateway.recordLogininfor(username,
                    LoginTypeEnum.LOGIN.getCode(),
                    LoginStatusEnum.FAIL.getCode(),
                    String.format("密码输入错误%s次",
                    loginSecurityGateway.getRetryCount(username)));
            throw new ServiceException("用户不存在/密码错误");
        } else {
            loginSecurityGateway.clearRetryCount(username);
        }
    }
}

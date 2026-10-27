package com.lemnt.auth.application.auth.executor;

import com.lemnt.auth.application.auth.cmd.UnLockCmd;
import com.lemnt.auth.domain.user.entity.LoginUser;
import com.lemnt.auth.domain.user.entity.UserInfo;
import com.lemnt.auth.domain.user.gateway.UserGateway;
import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.common.core.utils.StringUtils;
import com.lemnt.common.security.utils.SecurityUtils;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * 解锁屏幕执行器
 *
 * @author wangxx
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UnLockCmdExe {
    private final UserGateway userGateway;
    public Boolean execute(UnLockCmd cmd) {
        String username = SecurityUtils.getUsername();

        // 或密码为空 错误
        if (StringUtils.isEmpty(cmd.password())) {
            throw new ServiceException("密码不能为空");
        }
        // 查询用户信息
        LoginUser loginUser = userGateway.getUserInfo(username);
        if (Objects.isNull(loginUser)) {
            throw new ServiceException("用户不存在");
        }
        UserInfo user = loginUser.getDetail();
        if (!SecurityUtils.matchesPassword(cmd.password(), user.getPassword())) {
            throw new ServiceException("密码错误，请重新输入");
        }
        return true;
    }
}

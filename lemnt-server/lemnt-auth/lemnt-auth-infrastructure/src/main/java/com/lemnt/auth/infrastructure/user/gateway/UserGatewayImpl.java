package com.lemnt.auth.infrastructure.user.gateway;

import com.lemnt.auth.domain.user.gateway.UserGateway;
import com.lemnt.auth.domain.user.entity.LoginUser;
import com.lemnt.auth.infrastructure.user.converter.LoginUserConverter;
import com.lemnt.system.api.RemoteLogService;
import com.lemnt.system.api.RemoteUserService;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

/**
 * 用户网关实现
 *
 * @author wangxx
 */
@Service
@RequiredArgsConstructor
public class UserGatewayImpl implements UserGateway {

    @DubboReference
    private RemoteLogService remoteLogService;
    @DubboReference
    private RemoteUserService remoteUserService;

    @Override
    public LoginUser getUserInfo(String username) {
        return LoginUserConverter.to(remoteUserService.getUserInfo(username));
    }
}

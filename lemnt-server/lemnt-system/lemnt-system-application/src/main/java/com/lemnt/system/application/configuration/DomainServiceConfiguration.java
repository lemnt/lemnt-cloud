package com.lemnt.system.application.configuration;

import com.lemnt.system.domain.auth.gateway.RoleGateway;
import com.lemnt.system.domain.auth.gateway.UserGateway;
import com.lemnt.system.domain.auth.service.RoleDomainService;
import com.lemnt.system.domain.auth.service.UserDomainService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 领域服务配置。
 *
 * @author wangxx
 */
@Configuration(proxyBeanMethods = false)
public class DomainServiceConfiguration {
    @Bean
    public UserDomainService userDomainService(UserGateway userGateway) {
        return new UserDomainService(userGateway);
    }

    @Bean
    public RoleDomainService roleDomainService(RoleGateway roleGateway) {
        return new RoleDomainService(roleGateway);
    }
}

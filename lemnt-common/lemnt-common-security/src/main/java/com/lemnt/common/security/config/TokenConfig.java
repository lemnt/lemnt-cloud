package com.lemnt.common.security.config;

import com.lemnt.common.security.properties.TokenProperties;
import com.lemnt.common.security.utils.JwtUtils;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 令牌配置
 *
 * <p>各服务均通过 {@code scanBasePackages} 限定了扫描范围，本类需在
 * {@code META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports} 中登记才会生效。</p>
 *
 * @author wangxx
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(TokenProperties.class)
public class TokenConfig {

    /**
     * JWT 工具，密钥来自 {@link TokenProperties}，配置非法时应用启动阶段即失败
     */
    @Bean
    @ConditionalOnMissingBean
    public JwtUtils jwtUtils(TokenProperties tokenProperties) {
        return new JwtUtils(tokenProperties);
    }
}
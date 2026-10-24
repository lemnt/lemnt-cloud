package com.lemnt.common.adapter.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;

/**
 * MVC 请求对象绑定配置。
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class RequestBindingAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public GlobalRequestBindingAdvice globalRequestBindingAdvice() {
        return new GlobalRequestBindingAdvice();
    }
}

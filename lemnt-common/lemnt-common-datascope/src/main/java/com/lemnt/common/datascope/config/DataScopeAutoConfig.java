package com.lemnt.common.datascope.config;

import com.baomidou.mybatisplus.extension.plugins.handler.DataPermissionHandler;
import com.baomidou.mybatisplus.extension.plugins.inner.DataPermissionInterceptor;
import com.lemnt.common.datascope.handler.CustomDataPermissionHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 数据权限配置
 *
 * @author wangxx
 * @version 1.0.0
 * @date 2025/7/29 22:09
 */
@Configuration
@ConditionalOnClass(DataPermissionInterceptor.class)
public class DataScopeAutoConfig {

    @Bean
    public DataPermissionHandler dataPermissionHandler() {
        return new CustomDataPermissionHandler();
    }

    /**
     * 注册数据权限拦截器
     */
    @Bean
    public DataPermissionInterceptor dataPermissionInterceptor(DataPermissionHandler handler) {
        DataPermissionInterceptor dpi = new DataPermissionInterceptor();
        dpi.setDataPermissionHandler(handler);
        return dpi;
    }
}

package com.lemnt.common.sql.config;

import com.lemnt.common.sql.interceptor.SlowSqlInterceptor;
import com.lemnt.common.sql.properties.SqlMonitorProperties;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SQL 监控自动配置
 */
@Configuration
@EnableConfigurationProperties(SqlMonitorProperties.class)
@ConditionalOnProperty(
    prefix = "lemnt.sql.monitor",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true)
public class SqlMonitorAutoConfiguration {

    /**
     * 慢 SQL 拦截器
     */
    @Bean
    public SlowSqlInterceptor slowSqlInterceptor(
        SqlMonitorProperties properties, ObjectProvider<MeterRegistry> meterRegistryProvider) {
        return new SlowSqlInterceptor(properties, meterRegistryProvider.getIfAvailable());
    }
}

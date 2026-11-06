package com.lemnt.common.metrics.config;

import com.lemnt.common.metrics.binder.DataSourceMetricsBinder;
import com.lemnt.common.metrics.binder.RedissonMetricsBinder;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

/**
 * 指标监控自动配置
 * <p>
 * 自动注册连接池指标采集器，包括 Redisson 和 HikariCP
 * </p>
 */
@AutoConfiguration
@ConditionalOnBean(MeterRegistry.class)
public class MetricsAutoConfiguration {

    /**
     * Redisson 连接池指标采集器
     */
    @Bean
    @ConditionalOnClass(name = "org.redisson.api.RedissonClient")
    public RedissonMetricsBinder redissonMetricsBinder() {
        return new RedissonMetricsBinder();
    }

    /**
     * 数据源连接池指标采集器
     */
    @Bean
    @ConditionalOnClass(name = "com.baomidou.dynamic.datasource.DynamicRoutingDataSource")
    public DataSourceMetricsBinder dataSourceMetricsBinder() {
        return new DataSourceMetricsBinder();
    }
}

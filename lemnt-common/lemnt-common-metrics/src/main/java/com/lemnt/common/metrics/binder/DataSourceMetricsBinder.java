package com.lemnt.common.metrics.binder;

import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.zaxxer.hikari.HikariDataSource;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.springframework.beans.factory.ObjectProvider;

import java.util.Map;
import javax.sql.DataSource;

/**
 * 数据源连接池指标采集器
 * <p>
 * 采集 HikariCP 连接池的最大连接数和最小空闲连接数
 * </p>
 */
public class DataSourceMetricsBinder implements MeterBinder {

    private final ObjectProvider<DynamicRoutingDataSource> dataSourceProvider;

    public DataSourceMetricsBinder() {
        this.dataSourceProvider = null;
    }

    public DataSourceMetricsBinder(ObjectProvider<DynamicRoutingDataSource> dataSourceProvider) {
        this.dataSourceProvider = dataSourceProvider;
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        if (dataSourceProvider == null) {
            return;
        }

        DynamicRoutingDataSource dynamicDataSource = dataSourceProvider.getIfAvailable();
        if (dynamicDataSource == null) {
            return;
        }

        Map<String, DataSource> dataSources = dynamicDataSource.getDataSources();
        dataSources.forEach(
            (name, dataSource) -> {
                if (dataSource instanceof HikariDataSource hikariDataSource) {
                    bindHikariMetrics(registry, name, hikariDataSource);
                }
            });
    }

    private void bindHikariMetrics(MeterRegistry registry, String name, HikariDataSource hikari) {
        // 最大连接数
        Gauge.builder("hikaricp.connections.max", hikari, HikariDataSource::getMaximumPoolSize)
            .tag("pool", name)
            .description("连接池最大连接数")
            .register(registry);

        // 最小空闲连接数
        Gauge.builder("hikaricp.connections.min", hikari, HikariDataSource::getMinimumIdle)
            .tag("pool", name)
            .description("连接池最小空闲连接数")
            .register(registry);

        // 连接池名称
        Gauge.builder("hikaricp.pool.info", hikari, ds -> 1)
            .tag("pool", name)
            .tag("jdbcUrl", hikari.getJdbcUrl() != null ? hikari.getJdbcUrl() : "unknown")
            .description("连接池信息")
            .register(registry);
    }
}

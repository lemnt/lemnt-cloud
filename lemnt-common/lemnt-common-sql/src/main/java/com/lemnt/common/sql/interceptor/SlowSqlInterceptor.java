package com.lemnt.common.sql.interceptor;

import com.lemnt.common.sql.properties.SqlMonitorProperties;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import lombok.extern.slf4j.Slf4j;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * 慢 SQL 拦截器
 * <p>
 * 拦截 MyBatis 的查询和更新操作，记录执行时间，超过阈值的 SQL 打印警告日志
 * </p>
 */
@Slf4j
@Intercepts({
    @Signature(
        type = Executor.class,
        method = "update",
        args = {MappedStatement.class, Object.class}),
    @Signature(
        type = Executor.class,
        method = "query",
        args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class})
})
public class SlowSqlInterceptor implements Interceptor {

    private final SqlMonitorProperties properties;
    private final MeterRegistry meterRegistry;

    private Timer sqlTimer;
    private Counter slowSqlCounter;

    public SlowSqlInterceptor(SqlMonitorProperties properties, MeterRegistry meterRegistry) {
        this.properties = properties;
        this.meterRegistry = meterRegistry;
        if (meterRegistry != null) {
            this.sqlTimer =
                Timer.builder("db.sql.duration")
                    .description("SQL 执行耗时")
                    .publishPercentiles(0.5, 0.95, 0.99)
                    .register(meterRegistry);

            this.slowSqlCounter =
                Counter.builder("db.sql.slow.total")
                    .description("慢 SQL 执行总次数")
                    .register(meterRegistry);
        }
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        if (!properties.isEnabled()) {
            return invocation.proceed();
        }

        MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
        String sqlId = mappedStatement.getId();
        String sql = getSql(mappedStatement, invocation);

        long startTime = System.currentTimeMillis();
        try {
            return invocation.proceed();
        } finally {
            long duration = System.currentTimeMillis() - startTime;

            // 记录指标
            if (sqlTimer != null) {
                sqlTimer.record(duration, TimeUnit.MILLISECONDS);
            }

            // 慢 SQL 告警
            if (duration > properties.getSlowThresholdMs()) {
                if (properties.isLogSlowSql()) {
                    log.warn("[Slow SQL] 执行耗时: {}ms, sqlId: {}, sql: {}", duration, sqlId, sql);
                }
                if (slowSqlCounter != null) {
                    slowSqlCounter.increment();
                }
            }
        }
    }

    private String getSql(MappedStatement mappedStatement, Invocation invocation) {
        try {
            Object parameter = invocation.getArgs().length > 1 ? invocation.getArgs()[1] : null;
            BoundSql boundSql = mappedStatement.getBoundSql(parameter);
            return boundSql.getSql().replaceAll("\\s+", " ").trim();
        } catch (Exception e) {
            return "无法获取 SQL: " + e.getMessage();
        }
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        // 不需要额外配置
    }
}

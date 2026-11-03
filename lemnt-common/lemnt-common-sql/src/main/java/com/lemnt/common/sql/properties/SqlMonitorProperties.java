package com.lemnt.common.sql.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * SQL 监控配置属性
 */
@Data
@ConfigurationProperties(prefix = "lemnt.sql.monitor")
public class SqlMonitorProperties {

    /**
     * 是否启用 SQL 监控
     */
    private boolean enabled = true;

    /**
     * 慢 SQL 阈值（毫秒）
     */
    private long slowThresholdMs = 1000;

    /**
     * 是否记录慢 SQL 日志
     */
    private boolean logSlowSql = true;
}

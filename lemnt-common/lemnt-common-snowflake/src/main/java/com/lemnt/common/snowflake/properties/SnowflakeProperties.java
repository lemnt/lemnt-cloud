package com.lemnt.common.snowflake.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Data
@Validated
@ConfigurationProperties(prefix = "snowflake")
public class SnowflakeProperties {

    /**
     * 环境：prod/dev
     */
    private String env = "prod";

    /**
     * 机房ID（0~31）
     */
    @Min(0)
    @Max(31)
    private long datacenterId = 1L;

    /**
     * 最大workerId数量
     */
    @Min(0)
    @Max(31)
    private int maxWorkerId = 31;

    /**
     * Redis 租约过期时间（秒）
     */
    private long expireSeconds = 300;

    /**
     * 租约续约间隔（毫秒）
     */
    private long renewIntervalMs = 30000;
}

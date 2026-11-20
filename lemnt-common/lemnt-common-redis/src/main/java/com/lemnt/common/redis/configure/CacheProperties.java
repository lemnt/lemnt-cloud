package com.lemnt.common.redis.configure;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

/**
 * 缓存配置属性
 *
 * @author wangxx
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "lemnt-common.cache")
public class CacheProperties {

    /**
     * Caffeine 最大缓存条目数
     */
    private long maxSize = 1_000_000;

    /**
     * 缓存过期时间（秒）
     */
    private long ttlSeconds = 300;
}

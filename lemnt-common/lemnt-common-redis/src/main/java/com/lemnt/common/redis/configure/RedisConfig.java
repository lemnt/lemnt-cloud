package com.lemnt.common.redis.configure;

import com.lemnt.common.redis.serializer.Fastjson2RedisSerializer;
import com.lemnt.common.redis.serializer.RedisSerializer;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.data.redis.autoconfigure.DataRedisAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Redis 配置
 *
 * @author wangxx
 */
@Configuration
@EnableCaching
@AutoConfigureBefore(DataRedisAutoConfiguration.class)
@EnableConfigurationProperties(CacheProperties.class)
public class RedisConfig {

    private final CacheProperties cacheProperties;

    public RedisConfig(CacheProperties cacheProperties) {
        this.cacheProperties = cacheProperties;
    }

    /**
     * 默认 Redis 序列化器
     */
    @Bean
    @ConditionalOnMissingBean(RedisSerializer.class)
    public RedisSerializer redisSerializer() {
        return new Fastjson2RedisSerializer();
    }

    /**
     * CacheManager 配置
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager();
        manager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(cacheProperties.getMaxSize())
                .expireAfterWrite(cacheProperties.getTtlSeconds(), TimeUnit.SECONDS)
                .recordStats());
        return manager;
    }
}

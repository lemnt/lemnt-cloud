package com.lemnt.common.metrics.binder;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.ObjectProvider;

/**
 * Redisson 连接池指标采集器
 * <p>
 * 采集 Redisson 客户端的基本指标
 * </p>
 */
public class RedissonMetricsBinder implements MeterBinder {

    private final ObjectProvider<RedissonClient> redissonClientProvider;

    public RedissonMetricsBinder() {
        this.redissonClientProvider = null;
    }

    public RedissonMetricsBinder(ObjectProvider<RedissonClient> redissonClientProvider) {
        this.redissonClientProvider = redissonClientProvider;
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        if (redissonClientProvider == null) {
            return;
        }

        RedissonClient client = redissonClientProvider.getIfAvailable();
        if (client == null) {
            return;
        }

        // Redisson 客户端存在性指标
        Gauge.builder("redisson.client.active", () -> 1)
                .description("Redisson 客户端是否活跃")
                .register(registry);
    }
}

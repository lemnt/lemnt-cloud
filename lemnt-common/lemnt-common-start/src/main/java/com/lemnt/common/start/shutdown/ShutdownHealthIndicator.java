package com.lemnt.common.start.shutdown;

import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * 优雅停机健康指标
 *
 * @author wangxx
 */
@Component
public class ShutdownHealthIndicator implements HealthIndicator {

    private final ShutdownManager shutdownManager;

    public ShutdownHealthIndicator(ShutdownManager shutdownManager) {
        this.shutdownManager = shutdownManager;
    }

    @Override
    public Health health() {
        if (shutdownManager.isShuttingDown()) {
            return Health.down()
                    .withDetail("reason", "Application is shutting down gracefully")
                    .build();
        } else {
            return Health.up().build();
        }
    }
}

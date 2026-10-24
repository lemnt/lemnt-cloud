package com.lemnt.common.start.shutdown;

import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * 优雅停机状态管理
 *
 * @author wangxx
 */
@Component
public class ShutdownManager {

    /**
     * 标记实例是否处于下线状态
     */
    private volatile boolean shuttingDown = false;
    private final ApplicationContext context;

    public ShutdownManager(ApplicationContext context) {
        this.context = context;
    }

    /**
     * 开始拒绝新流量
     */
    public void startShutdown() {
        shuttingDown = true;
        // 核心：通知 Spring 拒绝新流量
        AvailabilityChangeEvent.publish(context, ReadinessState.REFUSING_TRAFFIC);
    }

    /**
     * 是否正在优雅停机
     */
    public boolean isShuttingDown() {
        return shuttingDown;
    }
}

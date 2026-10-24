package com.lemnt.common.start.shutdown;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

/**
 * 优雅停机控制
 * <p>
 * 1. 标记 ShutdownManager 下线状态
 * 2. 等待存量请求完成 + Gateway / Nacos 刷新
 * 3. 调用 NacosDeregister 摘除实例
 *
 * @author wangxx
 */
@Component
public class GracefulShutdownLifecycle implements SmartLifecycle {

    private static final Logger logger = LoggerFactory.getLogger(GracefulShutdownLifecycle.class);

    private final ShutdownManager shutdownManager;
    private volatile boolean running = true;

    public GracefulShutdownLifecycle(ShutdownManager shutdownManager) {
        this.shutdownManager = shutdownManager;
    }

    @Override
    public void start() {
        // 不需要启动逻辑
    }

    @Override
    public void stop() {
        logger.info(">>> 开始优雅下线，标记拒绝新流量");
        shutdownManager.startShutdown();
        running = false;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public int getPhase() {
        // 最大值，保证最后执行
        return Integer.MAX_VALUE;
    }
}

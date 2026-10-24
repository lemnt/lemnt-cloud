package com.lemnt.common.snowflake.core;

import com.lemnt.common.core.constant.CacheConstants;
import com.lemnt.common.snowflake.properties.SnowflakeProperties;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.SmartLifecycle;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.time.Duration;
import java.util.UUID;

/**
 * 雪花算法工作节点生成
 *
 * @author wangxx
 */
@Slf4j
public class WorkerIdAssigner implements SmartLifecycle {

    private final StringRedisTemplate stringRedisTemplate;
    private final SnowflakeProperties properties;
    private final String serviceName;

    @Getter
    private int workerId;
    private String workerKey;
    private final String instanceId;
    private volatile boolean running;

    public WorkerIdAssigner(StringRedisTemplate stringRedisTemplate,
                            SnowflakeProperties properties, String serviceName) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.properties = properties;
        this.serviceName = serviceName;
        this.instanceId = buildInstanceId();

        init();
        running = true;
    }

    @Override
    public void start() {
        running = true;
    }

    @Override
    public void stop() {
        if (!running) {
            return;
        }
        running = false;
        try {
            String val = stringRedisTemplate.opsForValue().get(workerKey);
            if (instanceId.equals(val)) {
                stringRedisTemplate.delete(workerKey);
                log.info("WorkerId released on shutdown: {}", workerId);
            }
        } catch (Exception e) {
            log.warn("Failed to release workerId on shutdown", e);
        }
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public int getPhase() {
        // 高于 LettuceConnectionFactory，确保关闭时先释放 WorkerId 租约
        return Integer.MAX_VALUE;
    }

    /**
     * 调度器续约
     */
    @Scheduled(fixedDelayString = "${snowflake.renew-interval-ms:30000}")
    public void renewLease() {
        if (!running || workerKey == null) {
            return;
        }
        try {
            String currentOwner = stringRedisTemplate.opsForValue().get(workerKey);
            // key 已过期或被其他实例抢占
            if (!instanceId.equals(currentOwner)) {
                log.warn("WorkerId ownership lost, re-assigning. workerId={}, expected={}, actual={}",
                        workerId, instanceId, currentOwner);
                init();
                log.warn("WorkerId re-assigned: {} | instance={}", workerId, instanceId);
                return;
            }
            // 续期
            Boolean success = stringRedisTemplate.expire(workerKey, Duration.ofSeconds(properties.getExpireSeconds()));
            if (!Boolean.TRUE.equals(success)) {
                log.warn("WorkerId lease renewal failed, re-assigning. workerId={}", workerId);
                init();
                log.warn("WorkerId re-assigned: {} | instance={}", workerId, instanceId);
                return;
            }
            if (log.isDebugEnabled()) {
                log.debug("WorkerId lease renewed: {} | instance={}", workerId, instanceId);
            }
        } catch (Exception e) {
            log.error("WorkerId lease renewal failed, application should exit", e);
            throw new IllegalStateException("Lost workerId ownership: " + workerId, e);
        }
    }

    private void init() {
        for (int i = 0; i < properties.getMaxWorkerId(); i++) {
            String key = buildKey(i);

            Boolean success = stringRedisTemplate.opsForValue().setIfAbsent(
                    key,
                    instanceId,
                    Duration.ofSeconds(properties.getExpireSeconds())
            );

            if (Boolean.TRUE.equals(success)) {
                this.workerId = i;
                this.workerKey = key;
                log.info("WorkerId assigned: {} | instance={}", workerId, instanceId);
                return;
            }
        }
        throw new IllegalStateException("No available workerId for service=" + serviceName);
    }

    private String buildKey(int workerId) {
        return String.format(CacheConstants.SNOWFLAKE_WORKER_KEY,
                properties.getEnv(),
                serviceName,
                properties.getDatacenterId(),
                workerId);
    }

    private String buildInstanceId() {
        try {
            String ip = InetAddress.getLocalHost().getHostAddress();
            String pid = ManagementFactory.getRuntimeMXBean().getName();
            return ip + "-" + pid;
        } catch (Exception e) {
            return UUID.randomUUID().toString();
        }
    }

}

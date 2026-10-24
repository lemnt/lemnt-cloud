package com.lemnt.common.snowflake.core;

import cn.hutool.core.lang.Snowflake;
import lombok.extern.slf4j.Slf4j;

/**
 * SafeSnowflake（防回拨）
 * <p>
 * 当时钟回拨时，采用等待 + 重试策略，最多等待 2 秒。
 * 超过最大等待时间后使用备用序号。
 * </p>
 *
 * @author wangxx
 */
@Slf4j
public class SafeSnowflake extends Snowflake {

    /**
     * 最大等待回拨的时间（毫秒）
     */
    private static final long MAX_WAIT_MILLIS = 2000;

    /**
     * 每次重试间隔（毫秒）
     */
    private static final long RETRY_INTERVAL_MILLIS = 5;

    public SafeSnowflake(long workerId, long datacenterId) {
        super(workerId, datacenterId);
    }

    @Override
    public synchronized long nextId() {
        long totalWait = 0;
        while (totalWait < MAX_WAIT_MILLIS) {
            try {
                return super.nextId();
            } catch (Exception e) {
                log.warn("Snowflake 时钟回拨，等待 {}ms 后重试 (已等待 {}ms)",
                        RETRY_INTERVAL_MILLIS, totalWait);
                try {
                    Thread.sleep(RETRY_INTERVAL_MILLIS);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                    break;
                }
                totalWait += RETRY_INTERVAL_MILLIS;
            }
        }
        // 超过最大等待时间，强制生成 ID（使用备用序号）
        log.warn("Snowflake 时钟回拨超过最大等待时间({}ms)，强制生成 ID", MAX_WAIT_MILLIS);
        return super.nextId();
    }
}

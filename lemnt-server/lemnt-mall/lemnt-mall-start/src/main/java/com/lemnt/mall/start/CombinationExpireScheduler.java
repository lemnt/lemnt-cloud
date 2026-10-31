package com.lemnt.mall.start;

import com.lemnt.mall.application.promotion.executor.CombinationExpireCmdExe;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** 拼团过期调度；虚拟成团或整团失败由应用服务处理。 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "lemnt.mall.scheduler.enabled", havingValue = "true")
public class CombinationExpireScheduler {
    private final CombinationExpireCmdExe expire;

    @Scheduled(fixedDelayString = "${lemnt.mall.scheduler.combination-expire-delay-ms:60000}")
    public void expireCombinations() {
        try {
            var count = expire.execute();
            if (count > 0) {
                log.info("拼团过期处理完成, count={}", count);
            }
        } catch (RuntimeException exception) {
            log.error("拼团过期处理失败", exception);
        }
    }
}

package com.lemnt.promotion.start;

import com.lemnt.promotion.application.promotion.executor.BargainExpireCmdExe;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** 砍价活动到期调度，进行中的记录统一失败。 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "lemnt.mall.scheduler.enabled", havingValue = "true")
public class BargainExpireScheduler {
    private final BargainExpireCmdExe expire;

    @Scheduled(fixedDelayString = "${lemnt.mall.scheduler.bargain-expire-delay-ms:60000}")
    public void expireBargains() {
        try {
            var count = expire.execute();
            if (count > 0) {
                log.info("砍价到期处理完成, count={}", count);
            }
        } catch (RuntimeException exception) {
            log.error("砍价到期处理失败", exception);
        }
    }
}

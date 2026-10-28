package com.lemnt.mall.start;

import com.lemnt.mall.application.coupon.executor.CouponExpireCmdExe;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** 优惠券过期调度，与订单锁定互不覆盖。 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "lemnt.mall.scheduler.enabled", havingValue = "true")
public class CouponExpireScheduler {
    private final CouponExpireCmdExe expire;

    /** 定时过期未用券；异常记录后交给下一次调度重试。 */
    @Scheduled(fixedDelayString = "${lemnt.mall.scheduler.coupon-expire-delay-ms:60000}")
    public void expireCoupons() {
        try {
            expire.execute();
        } catch (RuntimeException exception) {
            log.error("优惠券过期处理失败", exception);
        }
    }
}

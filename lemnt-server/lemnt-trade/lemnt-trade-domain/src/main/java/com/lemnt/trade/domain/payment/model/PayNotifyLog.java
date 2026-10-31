package com.lemnt.trade.domain.payment.model;

import java.time.LocalDateTime;

/** 支付结果派发的一次尝试日志。 */
public record PayNotifyLog(
        Long id,
        Long taskId,
        int notifyTimes,
        String status,
        String response,
        LocalDateTime createTime
) {
}

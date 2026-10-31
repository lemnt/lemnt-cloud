package com.lemnt.trade.domain.payment.model;

import java.time.LocalDateTime;

/** 支付结果派发任务。 */
public record PayNotifyTask(
        Long id,
        String type,
        String bizNo,
        String orderNo,
        String status,
        int notifyTimes,
        int maxNotifyTimes,
        LocalDateTime nextNotifyTime,
        LocalDateTime lastExecuteTime,
        String lastResponse,
        LocalDateTime createTime
) {
}

package com.lemnt.trade.application.payment.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

/** 派发任务应用输出。 */
public record PayNotifyTaskDTO(
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
) implements Serializable {
}

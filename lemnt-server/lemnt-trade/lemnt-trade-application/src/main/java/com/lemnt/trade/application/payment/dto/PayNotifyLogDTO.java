package com.lemnt.trade.application.payment.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

/** 派发尝试日志应用输出。 */
public record PayNotifyLogDTO(
        int notifyTimes,
        String status,
        String response,
        LocalDateTime createTime
) implements Serializable {
}

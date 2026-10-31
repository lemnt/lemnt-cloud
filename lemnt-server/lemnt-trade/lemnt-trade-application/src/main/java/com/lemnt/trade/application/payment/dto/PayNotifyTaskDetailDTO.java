package com.lemnt.trade.application.payment.dto;

import java.io.Serializable;
import java.util.List;

/** 派发任务详情与尝试日志。 */
public record PayNotifyTaskDetailDTO(
        PayNotifyTaskDTO task,
        List<PayNotifyLogDTO> logs
) implements Serializable {
    public PayNotifyTaskDetailDTO {
        logs = logs == null ? List.of() : List.copyOf(logs);
    }
}

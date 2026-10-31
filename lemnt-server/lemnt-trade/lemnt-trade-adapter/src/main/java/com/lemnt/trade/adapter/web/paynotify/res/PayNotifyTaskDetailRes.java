package com.lemnt.trade.adapter.web.paynotify.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.util.List;

/** 派发任务详情与日志响应。 */
@Schema(description = "派发任务详情")
public record PayNotifyTaskDetailRes(
        @Schema(description = "任务")
        PayNotifyTaskRes task,
        @Schema(description = "尝试日志")
        List<PayNotifyLogRes> logs
) implements Serializable {
    public PayNotifyTaskDetailRes {
        logs = logs == null ? List.of() : List.copyOf(logs);
    }
}

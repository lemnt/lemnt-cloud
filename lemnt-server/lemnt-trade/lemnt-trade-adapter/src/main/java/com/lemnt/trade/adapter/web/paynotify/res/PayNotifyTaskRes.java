package com.lemnt.trade.adapter.web.paynotify.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.time.LocalDateTime;

/** 派发任务响应。 */
@Schema(description = "派发任务响应")
public record PayNotifyTaskRes(
        @Schema(description = "任务编号")
        Long id,
        @Schema(description = "类型：PAY/REFUND")
        String type,
        @Schema(description = "业务单号")
        String bizNo,
        @Schema(description = "订单编号")
        String orderNo,
        @Schema(description = "状态")
        String status,
        @Schema(description = "已尝试次数")
        int notifyTimes,
        @Schema(description = "最大尝试次数")
        int maxNotifyTimes,
        @Schema(description = "下次派发时间")
        LocalDateTime nextNotifyTime,
        @Schema(description = "最后执行时间")
        LocalDateTime lastExecuteTime,
        @Schema(description = "最后响应或失败信息")
        String lastResponse,
        @Schema(description = "创建时间")
        LocalDateTime createTime
) implements Serializable {
}

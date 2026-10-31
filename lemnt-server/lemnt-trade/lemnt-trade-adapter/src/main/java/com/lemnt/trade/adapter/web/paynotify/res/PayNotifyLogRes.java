package com.lemnt.trade.adapter.web.paynotify.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.time.LocalDateTime;

/** 派发尝试日志响应。 */
@Schema(description = "派发尝试日志")
public record PayNotifyLogRes(
        @Schema(description = "第几次尝试")
        int notifyTimes,
        @Schema(description = "结果：SUCCESS/FAILED")
        String status,
        @Schema(description = "响应或失败信息")
        String response,
        @Schema(description = "创建时间")
        LocalDateTime createTime
) implements Serializable {
}

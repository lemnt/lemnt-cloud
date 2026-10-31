package com.lemnt.trade.adapter.web.paynotify.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

/** 派发任务分页请求。 */
@Schema(description = "派发任务分页请求")
public record PayNotifyTaskPageReq(
        @Schema(description = "类型：PAY/REFUND")
        @Size(max = 16) String type,
        @Schema(description = "业务单号")
        @Size(max = 40) String bizNo,
        @Schema(description = "订单编号")
        @Size(max = 40) String orderNo,
        @Schema(description = "状态：PENDING/SUCCESS/FAILED")
        @Size(max = 20) String status,
        @Schema(description = "页码默认1")
        @Min(1) @Max(1000000) Integer pageNum,
        @Schema(description = "每页数量默认20")
        @Min(1) @Max(100) Integer pageSize
) implements Serializable {
}

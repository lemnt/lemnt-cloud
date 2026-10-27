package com.lemnt.trade.adapter.web.fulfillment.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/**
 * 退款分页请求。
 */
@Schema(description = "Refund分页查询")
public record RefundPageReq(
        @Schema(description = "精确订单编号，可空")
        @Size(max = 40)
        String orderNo,
        @Schema(description = "状态筛选，可空")
        @Size(max = 20)
        String status,
        @Schema(description = "页码，从1开始，默认1")
        @Min(1)
        @Max(1000000)
        Integer pageNum,
        @Schema(description = "每页数量，默认20，最大100")
        @Min(1)
        @Max(100)
        Integer pageSize
) implements Serializable {}

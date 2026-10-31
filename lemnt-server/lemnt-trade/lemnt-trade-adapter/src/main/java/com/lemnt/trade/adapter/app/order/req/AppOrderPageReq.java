package com.lemnt.trade.adapter.app.order.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

/** 会员订单分页请求：不接受 memberId，identity 由认证上下文提供。 */
@Schema(description = "会员订单分页请求")
public record AppOrderPageReq(
        @Schema(description = "订单状态")
        @Size(max = 20)
        String status,
        @Schema(description = "评价状态，区分待评价与已完成")
        Boolean commentStatus,
        @Schema(description = "页码默认1")
        @Min(1)
        @Max(1000000)
        Integer pageNum,
        @Schema(description = "每页数量默认10")
        @Min(1)
        @Max(100)
        Integer pageSize
) implements Serializable {
}

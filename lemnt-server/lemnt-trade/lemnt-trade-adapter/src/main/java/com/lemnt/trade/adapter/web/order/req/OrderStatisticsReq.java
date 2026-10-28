package com.lemnt.trade.adapter.web.order.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Size;
import java.io.Serializable;

/** 订单统计请求。 */
@Schema(description = "订单统计请求")
public record OrderStatisticsReq(
        @Schema(description = "可空，精确订单编号，不传统计全部订单")
        @Size(max = 40)
        String orderNo
) implements Serializable {
}

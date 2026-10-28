package com.lemnt.trade.adapter.web.payment.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/** 后台Payment分页查询请求。 */
@Schema(description = "后台Payment分页查询请求")
public record PaymentPageReq(
        @Schema(description = "精确支付单编号")
        @Size(max = 40)
        String paymentNo,
        @Schema(description = "精确订单编号")
        @Size(max = 40)
        String orderNo,
        @Schema(description = "支付渠道编码：ALIPAY、WECHAT")
        @Size(max = 32)
        String channel,
        @Schema(description = "状态精确筛选")
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
) implements Serializable {
}

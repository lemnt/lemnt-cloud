package com.lemnt.trade.adapter.web.paymentrefund.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/** 后台PaymentRefund分页查询请求。 */
@Schema(description = "后台PaymentRefund分页查询请求")
public record PaymentRefundPageReq(
        @Schema(description = "精确售后单编号")
        @Size(max = 40)
        String afterSaleNo,
        @Schema(description = "精确原支付单编号")
        @Size(max = 40)
        String paymentNo,
        @Schema(description = "退款状态：WAITING、SUCCESS、FAILED")
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

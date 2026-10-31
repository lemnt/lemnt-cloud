package com.lemnt.trade.adapter.app.recharge.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

/** App 本人充值单分页请求。 */
@Schema(description = "本人充值单分页请求")
public record AppRechargePageReq(
        @Schema(description = "状态：PENDING/PAID/REFUNDED")
        @Size(max = 20) String status,
        @Schema(description = "页码默认1")
        @Min(1) @Max(1000000) Integer pageNum,
        @Schema(description = "每页数量默认10")
        @Min(1) @Max(100) Integer pageSize
) implements Serializable {
}

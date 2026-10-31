package com.lemnt.trade.adapter.app.aftersale.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

/** 会员售后分页请求：不接受 memberId。 */
@Schema(description = "会员售后分页请求")
public record AppAfterSalePageReq(
        @Schema(description = "精确订单编号")
        @Size(max = 40)
        String orderNo,
        @Schema(description = "售后状态")
        @Size(max = 20)
        String status,
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

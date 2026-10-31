package com.lemnt.trade.adapter.web.paychannel.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

/** 支付渠道配置分页请求。 */
@Schema(description = "支付渠道配置分页请求")
public record PayChannelPageReq(
        @Schema(description = "所属支付应用编号")
        @Positive Long appId,
        @Schema(description = "渠道编码，模糊")
        @Size(max = 32) String code,
        @Schema(description = "状态：0启用、1停用")
        @Min(0) @Max(1) Integer status,
        @Schema(description = "页码默认1")
        @Min(1) @Max(1000000) Integer pageNum,
        @Schema(description = "每页数量默认20")
        @Min(1) @Max(100) Integer pageSize
) implements Serializable {
}

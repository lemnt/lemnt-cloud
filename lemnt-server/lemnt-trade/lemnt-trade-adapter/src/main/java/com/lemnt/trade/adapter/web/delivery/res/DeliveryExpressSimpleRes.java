package com.lemnt.trade.adapter.web.delivery.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

/** 配送选择列表响应。 */
@Schema(description = "配送选择列表响应")
public record DeliveryExpressSimpleRes(
        @Schema(description = "编号")
        Long id,
        @Schema(description = "快递公司编码")
        String code,
        @Schema(description = "快递公司名称")
        String name
) implements Serializable {
}

package com.lemnt.trade.adapter.web.delivery.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

/** 配送选择列表响应。 */
@Schema(description = "配送选择列表响应")
public record DeliveryExpressTemplateSimpleRes(
        @Schema(description = "编号")
        Long id,
        @Schema(description = "模板名称")
        String name
) implements Serializable {
}

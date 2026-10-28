package com.lemnt.trade.adapter.web.delivery.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

/** 配送详情响应。 */
@Schema(description = "配送详情响应")
public record DeliveryExpressDetailRes(
        @Schema(description = "编号")
        Long id,
        @Schema(description = "快递公司编码")
        String code,
        @Schema(description = "快递公司名称")
        String name,
        @Schema(description = "图标地址")
        String logo,
        @Schema(description = "排序")
        Integer sort,
        @Schema(description = "状态，0 启用、1 停用")
        Integer status
) implements Serializable {
}

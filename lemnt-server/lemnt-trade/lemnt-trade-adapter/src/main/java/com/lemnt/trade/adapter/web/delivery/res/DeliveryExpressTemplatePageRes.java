package com.lemnt.trade.adapter.web.delivery.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

/** 配送分页响应。 */
@Schema(description = "配送分页响应")
public record DeliveryExpressTemplatePageRes(
        @Schema(description = "编号")
        Long id,
        @Schema(description = "模板名称")
        String name,
        @Schema(description = "计费方式，1 件数、2 重量千克、3 体积立方米")
        Integer chargeMode,
        @Schema(description = "排序")
        Integer sort
) implements Serializable {
}

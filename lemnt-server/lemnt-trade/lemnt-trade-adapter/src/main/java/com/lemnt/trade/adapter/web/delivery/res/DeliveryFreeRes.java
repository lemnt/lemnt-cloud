package com.lemnt.trade.adapter.web.delivery.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.util.List;

/** 包邮规则响应。 */
@Schema(description = "包邮规则响应")
public record DeliveryFreeRes(
        @Schema(description = "编号")
        Long id,
        @Schema(description = "模板编号")
        Long templateId,
        @Schema(description = "配送区域编号集合")
        List<Integer> areaIds,
        @Schema(description = "包邮金额门槛，分")
        Integer freePrice,
        @Schema(description = "包邮计费量门槛")
        Integer freeCount
) implements Serializable {
}

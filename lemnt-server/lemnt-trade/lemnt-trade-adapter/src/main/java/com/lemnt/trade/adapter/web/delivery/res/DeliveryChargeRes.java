package com.lemnt.trade.adapter.web.delivery.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/** 计费规则响应。 */
@Schema(description = "计费规则响应")
public record DeliveryChargeRes(
        @Schema(description = "编号")
        Long id,
        @Schema(description = "模板编号")
        Long templateId,
        @Schema(description = "配送区域编号集合")
        List<Integer> areaIds,
        @Schema(description = "模板计费方式快照")
        Integer chargeMode,
        @Schema(description = "首量")
        BigDecimal startCount,
        @Schema(description = "首价，分")
        Integer startPrice,
        @Schema(description = "续量")
        BigDecimal extraCount,
        @Schema(description = "续价，分")
        Integer extraPrice
) implements Serializable {
}

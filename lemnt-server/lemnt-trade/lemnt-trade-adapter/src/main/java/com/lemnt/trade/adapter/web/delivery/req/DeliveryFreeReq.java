package com.lemnt.trade.adapter.web.delivery.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

/** 包邮规则请求。 */
@Schema(description = "包邮规则请求")
public record DeliveryFreeReq(
        @Schema(description = "编号")
        @Positive
        Long id,
        @Schema(description = "配送区域编号集合")
        @NotNull @Size(max = 10000)
        List<@NotNull @Positive Integer> areaIds,
        @Schema(description = "包邮金额门槛，分")
        @NotNull
        @DecimalMin("0")
        Integer freePrice,
        @Schema(description = "包邮计费量门槛")
        @NotNull
        @DecimalMin("0")
        Integer freeCount
) implements Serializable {
}

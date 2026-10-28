package com.lemnt.trade.adapter.web.delivery.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/** 计费规则请求。 */
@Schema(description = "计费规则请求")
public record DeliveryChargeReq(
        @Schema(description = "编号")
        @Positive
        Long id,
        @Schema(description = "配送区域编号集合")
        @NotNull @Size(max = 10000)
        List<@NotNull @Positive Integer> areaIds,
        @Schema(description = "首量")
        @NotNull
        @DecimalMin("0.000001")
        BigDecimal startCount,
        @Schema(description = "首价，分")
        @NotNull
        @DecimalMin("0")
        Integer startPrice,
        @Schema(description = "续量")
        @NotNull
        @DecimalMin("0.000001")
        BigDecimal extraCount,
        @Schema(description = "续价，分")
        @NotNull
        @DecimalMin("0")
        Integer extraPrice
) implements Serializable {
}

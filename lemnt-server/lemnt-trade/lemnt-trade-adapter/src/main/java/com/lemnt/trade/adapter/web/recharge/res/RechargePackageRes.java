package com.lemnt.trade.adapter.web.recharge.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 充值套餐响应。 */
@Schema(description = "充值套餐响应")
public record RechargePackageRes(
        @Schema(description = "套餐编号")
        Long id,
        @Schema(description = "套餐名")
        String name,
        @Schema(description = "支付金额，元")
        BigDecimal payPrice,
        @Schema(description = "赠送金额，元")
        BigDecimal bonusPrice,
        @Schema(description = "状态：0启用、1停用")
        Integer status,
        @Schema(description = "创建时间")
        LocalDateTime createTime
) implements Serializable {
}

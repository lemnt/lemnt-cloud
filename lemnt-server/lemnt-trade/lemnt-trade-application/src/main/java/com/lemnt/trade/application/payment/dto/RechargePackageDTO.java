package com.lemnt.trade.application.payment.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 充值套餐应用输出。 */
public record RechargePackageDTO(
        Long id,
        String name,
        BigDecimal payPrice,
        BigDecimal bonusPrice,
        Integer status,
        LocalDateTime createTime
) implements Serializable {
}

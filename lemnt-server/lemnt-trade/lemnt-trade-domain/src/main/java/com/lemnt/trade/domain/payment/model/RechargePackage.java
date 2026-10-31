package com.lemnt.trade.domain.payment.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 充值套餐，金额单位元。状态 0 启用、1 停用。 */
public record RechargePackage(
        Long id,
        String name,
        BigDecimal payPrice,
        BigDecimal bonusPrice,
        Integer status,
        LocalDateTime createTime
) {
}

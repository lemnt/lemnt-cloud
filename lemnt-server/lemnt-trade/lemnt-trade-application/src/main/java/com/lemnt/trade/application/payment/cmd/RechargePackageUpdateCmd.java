package com.lemnt.trade.application.payment.cmd;

import java.io.Serializable;
import java.math.BigDecimal;

/** 修改充值套餐。 */
public record RechargePackageUpdateCmd(
        Long id,
        String name,
        BigDecimal payPrice,
        BigDecimal bonusPrice,
        Integer status
) implements Serializable {
}

package com.lemnt.trade.application.payment.cmd;

import java.io.Serializable;
import java.math.BigDecimal;

/** 新增充值套餐。 */
public record RechargePackageCreateCmd(
        String name,
        BigDecimal payPrice,
        BigDecimal bonusPrice,
        Integer status
) implements Serializable {
}

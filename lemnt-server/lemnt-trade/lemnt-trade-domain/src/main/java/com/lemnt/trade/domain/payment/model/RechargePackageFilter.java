package com.lemnt.trade.domain.payment.model;

import java.io.Serializable;

/** 充值套餐分页筛选。 */
public record RechargePackageFilter(
        String name,
        Integer status,
        int pageNum,
        int pageSize
) implements Serializable {
}

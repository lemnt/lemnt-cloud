package com.lemnt.trade.application.payment.qry;

import java.io.Serializable;

/** 充值套餐分页查询。 */
public record RechargePackagePageQry(
        String name,
        Integer status,
        int pageNum,
        int pageSize
) implements Serializable {
}

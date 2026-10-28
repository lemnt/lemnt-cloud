package com.lemnt.trade.application.aftersale.cmd;

import java.io.Serializable;

/** 拒绝退货收货。 */
public record AfterSaleRefuseCmd(
        String afterSaleNo,
        String reason
) implements Serializable {
}

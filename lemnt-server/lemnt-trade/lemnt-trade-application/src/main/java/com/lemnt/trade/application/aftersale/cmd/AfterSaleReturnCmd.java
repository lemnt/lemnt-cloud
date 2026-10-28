package com.lemnt.trade.application.aftersale.cmd;

import java.io.Serializable;

/** 登记退货物流。 */
public record AfterSaleReturnCmd(
        String afterSaleNo,
        String carrierCode,
        String trackingNo
) implements Serializable {
}

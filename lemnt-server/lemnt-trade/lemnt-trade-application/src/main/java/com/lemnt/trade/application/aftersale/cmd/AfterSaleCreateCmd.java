package com.lemnt.trade.application.aftersale.cmd;

import java.io.Serializable;

/** 商品售后申请。 */
public record AfterSaleCreateCmd(
        String orderNo,
        String requestNo,
        Long orderItemId,
        Integer quantity,
        String type,
        String reason
) implements Serializable {
}

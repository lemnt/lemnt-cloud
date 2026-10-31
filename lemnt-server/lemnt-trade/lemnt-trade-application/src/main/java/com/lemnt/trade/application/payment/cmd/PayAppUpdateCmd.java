package com.lemnt.trade.application.payment.cmd;

import java.io.Serializable;

/** 修改支付应用，应用标识不可修改。 */
public record PayAppUpdateCmd(
        Long id,
        String name,
        Integer status,
        String remark,
        String orderNotifyUrl,
        String refundNotifyUrl
) implements Serializable {
}

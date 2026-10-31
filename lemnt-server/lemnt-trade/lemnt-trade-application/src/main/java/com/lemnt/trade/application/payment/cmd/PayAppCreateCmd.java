package com.lemnt.trade.application.payment.cmd;

import java.io.Serializable;

/** 新增支付应用。 */
public record PayAppCreateCmd(
        String appKey,
        String name,
        Integer status,
        String remark,
        String orderNotifyUrl,
        String refundNotifyUrl
) implements Serializable {
}

package com.lemnt.trade.application.order.cmd;

import java.io.Serializable;

/** 订单备注修改。 */
public record OrderRemarkUpdateCmd(
        String orderNo,
        String remark
) implements Serializable {
}

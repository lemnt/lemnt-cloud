package com.lemnt.trade.application.payment.cmd;

import java.io.Serializable;

/** 会员发起订单支付命令，渠道与应用由客户端选择，金额由服务端订单确定。 */
public record PaymentPrepayCmd(
        Long memberId,
        String orderNo,
        String appKey,
        String channel
) implements Serializable {
}

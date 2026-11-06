package com.lemnt.pay.application.payment.cmd;

import java.io.Serializable;

/**
 * 会员发起订单支付命令，渠道与应用由客户端选择，金额与付款人标识由服务端确定。
 */
public record PaymentPrepayCmd(Long memberId, String orderNo, String appKey, String channel)
    implements Serializable {
}

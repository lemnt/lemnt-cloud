package com.lemnt.pay.client.bo;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 原售后请求标识与固定金额，必须再核对订单服务冻结计划。
 */
public record PaymentRefundBo(
    String afterSaleNo,
    String orderNo,
    String paymentNo,
    Long memberId,
    BigDecimal amount,
    Integer ruleVersion)
    implements Serializable {
}

package com.lemnt.order.client.dto;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 订单服务提供的原订单支付快照，不包含当前商品或当前优惠配置。
 */
public record OrderPaymentSnapshotDTO(
    String orderNo, Long memberId, BigDecimal totalAmount, String status)
    implements Serializable {
}

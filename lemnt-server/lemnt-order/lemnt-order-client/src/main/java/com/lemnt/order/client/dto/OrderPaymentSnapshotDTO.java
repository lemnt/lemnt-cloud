package com.lemnt.order.client.dto;

/** 订单服务提供的原订单支付快照，不包含当前商品或当前优惠配置。 */
public record OrderPaymentSnapshotDTO(
        String orderNo, Long memberId, java.math.BigDecimal totalAmount, String status)
        implements java.io.Serializable {}

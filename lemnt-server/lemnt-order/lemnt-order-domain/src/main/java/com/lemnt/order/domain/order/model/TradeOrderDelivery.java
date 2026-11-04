package com.lemnt.order.domain.order.model;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 订单配送不可变快照，金额元。
 */
public record TradeOrderDelivery(
    Long addressId,
    Integer deliveryType,
    String receiverName,
    String receiverMobile,
    Long receiverAreaId,
    String receiverDetailAddress,
    BigDecimal deliveryPrice,
    Long pickUpStoreId,
    String pickUpVerifyCode)
    implements Serializable {
    /**
     * 兼容普通快递订单。
     */
    public TradeOrderDelivery(
        Long addressId,
        Integer deliveryType,
        String receiverName,
        String receiverMobile,
        Long receiverAreaId,
        String receiverDetailAddress,
        BigDecimal deliveryPrice) {
        this(
            addressId,
            deliveryType,
            receiverName,
            receiverMobile,
            receiverAreaId,
            receiverDetailAddress,
            deliveryPrice,
            null,
            null);
    }
}

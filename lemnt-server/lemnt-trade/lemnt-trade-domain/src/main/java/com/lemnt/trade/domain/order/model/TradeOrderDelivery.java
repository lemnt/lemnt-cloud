package com.lemnt.trade.domain.order.model;

import java.io.Serializable;
import java.math.BigDecimal;

/** 订单配送不可变快照，金额元。 */
public record TradeOrderDelivery(
        Long addressId,
        Integer deliveryType,
        String receiverName,
        String receiverMobile,
        Long receiverAreaId,
        String receiverDetailAddress,
        BigDecimal deliveryPrice
) implements Serializable {
}

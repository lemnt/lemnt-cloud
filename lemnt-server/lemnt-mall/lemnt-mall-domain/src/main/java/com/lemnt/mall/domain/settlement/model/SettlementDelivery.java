package com.lemnt.mall.domain.settlement.model;

import java.io.Serializable;
import java.math.BigDecimal;

/** 快递收件及运费不可变快照，金额元。 */
public record SettlementDelivery(
        Long addressId,
        Integer deliveryType,
        String receiverName,
        String receiverMobile,
        Long receiverAreaId,
        String receiverDetailAddress,
        BigDecimal deliveryPrice
) implements Serializable {
}

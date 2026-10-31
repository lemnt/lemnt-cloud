package com.lemnt.mall.application.settlement.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public record SettlementDeliveryDTO(
        Long addressId,
        Integer deliveryType,
        String receiverName,
        String receiverMobile,
        Long receiverAreaId,
        String receiverDetailAddress,
        BigDecimal deliveryPrice
) implements Serializable {
}

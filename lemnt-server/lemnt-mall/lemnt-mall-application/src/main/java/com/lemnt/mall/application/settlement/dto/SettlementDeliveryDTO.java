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
        BigDecimal deliveryPrice,
        Long pickUpStoreId
) implements Serializable {
    /** 兼容普通快递快照。 */
    public SettlementDeliveryDTO(Long addressId, Integer deliveryType, String receiverName,
            String receiverMobile, Long receiverAreaId, String receiverDetailAddress,
            BigDecimal deliveryPrice) {
        this(addressId, deliveryType, receiverName, receiverMobile, receiverAreaId,
                receiverDetailAddress, deliveryPrice, null);
    }
}

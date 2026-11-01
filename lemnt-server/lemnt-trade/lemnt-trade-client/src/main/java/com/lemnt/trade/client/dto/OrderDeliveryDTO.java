package com.lemnt.trade.client.dto;

import java.io.Serializable;
import java.math.BigDecimal;

/** 跨服务配送快照，金额元。 */
public record OrderDeliveryDTO(
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
    public OrderDeliveryDTO(Long addressId, Integer deliveryType, String receiverName,
            String receiverMobile, Long receiverAreaId, String receiverDetailAddress,
            BigDecimal deliveryPrice) {
        this(addressId, deliveryType, receiverName, receiverMobile, receiverAreaId,
                receiverDetailAddress, deliveryPrice, null);
    }
}

package com.lemnt.promotion.client.dto;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 快递收件及运费不可变快照，金额元。
 */
public record PromotionDeliveryDTO(
    Long addressId,
    Integer deliveryType,
    String receiverName,
    String receiverMobile,
    Long receiverAreaId,
    String receiverDetailAddress,
    BigDecimal deliveryPrice,
    Long pickUpStoreId)
    implements Serializable {
}

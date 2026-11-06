package com.lemnt.order.application.pickup.dto;

import java.io.Serializable;

/**
 * 自提订单核销信息。
 */
public record PickUpOrderDTO(
    String orderNo,
    String status,
    Long storeId,
    String storeName,
    String storePhone,
    String storeAddress,
    String verifyCode)
    implements Serializable {
}

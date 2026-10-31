package com.lemnt.trade.application.fulfillment.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

/** 物流单列表结果，不含收件信息。 */
public record ShipmentSummaryDTO(
        String orderNo,
        String carrierCode,
        String trackingNo,
        LocalDateTime shippedAt,
        LocalDateTime receivedAt
) implements Serializable {
}

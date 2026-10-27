package com.lemnt.trade.application.fulfillment.dto;

import java.time.LocalDateTime;

/**
 * 发货数据传输对象。
 */
public record ShipmentDTO(
        String orderNo,
        String requestNo,
        String carrierCode,
        String trackingNo,
        String recipientName,
        String phone,
        String address,
        LocalDateTime shippedAt,
        LocalDateTime receivedAt
) implements java.io.Serializable {}

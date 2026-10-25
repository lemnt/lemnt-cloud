package com.lemnt.trade.application.fulfillment.dto;

public record ShipmentDTO(
        String orderNo,
        String requestNo,
        String carrierCode,
        String trackingNo,
        String recipientName,
        String phone,
        String address,
        java.time.LocalDateTime shippedAt,
        java.time.LocalDateTime receivedAt) {}

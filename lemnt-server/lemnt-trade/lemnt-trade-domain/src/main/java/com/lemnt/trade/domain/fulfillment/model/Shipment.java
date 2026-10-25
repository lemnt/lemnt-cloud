package com.lemnt.trade.domain.fulfillment.model;

public record Shipment(
        String orderNo,
        String requestNo,
        String fingerprint,
        String carrierCode,
        String trackingNo,
        String recipientName,
        String phone,
        String address,
        java.time.LocalDateTime shippedAt,
        java.time.LocalDateTime receivedAt) {}

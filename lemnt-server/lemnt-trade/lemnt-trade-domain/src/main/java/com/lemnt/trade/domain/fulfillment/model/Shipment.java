package com.lemnt.trade.domain.fulfillment.model;

import java.time.LocalDateTime;

/**
 * 发货。
 */
public record Shipment(
        String orderNo,
        String requestNo,
        String fingerprint,
        String carrierCode,
        String trackingNo,
        String recipientName,
        String phone,
        String address,
        LocalDateTime shippedAt,
        LocalDateTime receivedAt
) {}

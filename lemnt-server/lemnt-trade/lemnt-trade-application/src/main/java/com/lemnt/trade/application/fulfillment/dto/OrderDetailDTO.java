package com.lemnt.trade.application.fulfillment.dto;

public record OrderDetailDTO(
        String orderNo,
        Long memberId,
        java.math.BigDecimal totalAmount,
        String status,
        boolean inventoryConfirmed,
        boolean activeRefund,
        java.util.List<com.lemnt.trade.application.checkout.dto.CheckoutItemDTO> items,
        ShipmentDTO shipment) {}

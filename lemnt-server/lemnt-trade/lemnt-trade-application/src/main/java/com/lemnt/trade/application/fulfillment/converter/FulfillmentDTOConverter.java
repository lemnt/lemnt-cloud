package com.lemnt.trade.application.fulfillment.converter;

import com.lemnt.trade.application.fulfillment.dto.*;
import com.lemnt.trade.domain.fulfillment.model.*;

public final class FulfillmentDTOConverter {
    private FulfillmentDTOConverter() {}

    public static ShipmentDTO shipment(Shipment value) {
        return value == null
                ? null
                : new ShipmentDTO(
                        value.orderNo(),
                        value.requestNo(),
                        value.carrierCode(),
                        value.trackingNo(),
                        value.recipientName(),
                        value.phone(),
                        value.address(),
                        value.shippedAt(),
                        value.receivedAt());
    }

    public static RefundRequestDTO refund(RefundRequest value) {
        return value == null
                ? null
                : new RefundRequestDTO(
                        value.refundNo(),
                        value.orderNo(),
                        value.requestNo(),
                        value.memberId(),
                        value.paymentNo(),
                        value.amount(),
                        value.reason(),
                        value.status(),
                        value.decisionReason(),
                        value.requestedAt(),
                        value.decidedAt());
    }

    public static OrderSummaryDTO summary(OrderSummary value) {
        return new OrderSummaryDTO(
                value.orderNo(),
                value.memberId(),
                value.totalAmount(),
                value.status(),
                value.createTime(),
                value.inventoryConfirmed(),
                value.activeRefund(),
                value.carrierCode(),
                value.trackingNo(),
                value.shippedAt(),
                value.receivedAt());
    }
}

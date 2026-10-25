package com.lemnt.trade.adapter.web.fulfillment.converter;

import com.lemnt.trade.adapter.web.fulfillment.res.*;
import com.lemnt.trade.application.fulfillment.dto.*;
import com.lemnt.trade.adapter.web.checkout.converter.CheckoutResConverter;

public final class FulfillmentResConverter {
    private FulfillmentResConverter() {}

    public static ShipmentRes shipment(ShipmentDTO v) {
        return v == null
                ? null
                : new ShipmentRes(
                        v.orderNo(),
                        v.requestNo(),
                        v.carrierCode(),
                        v.trackingNo(),
                        v.recipientName(),
                        v.phone(),
                        v.address(),
                        v.shippedAt(),
                        v.receivedAt());
    }

    public static RefundRequestRes refund(RefundRequestDTO v) {
        return v == null
                ? null
                : new RefundRequestRes(
                        v.refundNo(),
                        v.orderNo(),
                        v.requestNo(),
                        v.memberId(),
                        v.paymentNo(),
                        v.amount(),
                        v.reason(),
                        v.status(),
                        v.decisionReason(),
                        v.requestedAt(),
                        v.decidedAt());
    }

    public static OrderSummaryRes summary(OrderSummaryDTO v) {
        return v == null
                ? null
                : new OrderSummaryRes(
                        v.orderNo(),
                        v.memberId(),
                        v.totalAmount(),
                        v.status(),
                        v.createTime(),
                        v.inventoryConfirmed(),
                        v.activeRefund(),
                        v.carrierCode(),
                        v.trackingNo(),
                        v.shippedAt(),
                        v.receivedAt());
    }

    public static OrderDetailRes detail(OrderDetailDTO v) {
        return v == null
                ? null
                : new OrderDetailRes(
                        v.orderNo(),
                        v.memberId(),
                        v.totalAmount(),
                        v.status(),
                        v.inventoryConfirmed(),
                        v.activeRefund(),
                        v.items().stream().map(CheckoutResConverter::checkoutItem).toList(),
                        shipment(v.shipment()));
    }

    public static OrderPageRes orders(OrderPageDTO v) {
        return v == null
                ? null
                : new OrderPageRes(
                        v.records().stream().map(FulfillmentResConverter::summary).toList(),
                        v.total());
    }

    public static RefundPageRes refunds(RefundPageDTO v) {
        return v == null
                ? null
                : new RefundPageRes(
                        v.records().stream().map(FulfillmentResConverter::refund).toList(),
                        v.total());
    }
}

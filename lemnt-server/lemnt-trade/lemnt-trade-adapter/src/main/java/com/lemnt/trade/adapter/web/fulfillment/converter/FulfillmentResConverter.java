package com.lemnt.trade.adapter.web.fulfillment.converter;

import com.lemnt.trade.adapter.web.fulfillment.res.OrderDetailRes;
import com.lemnt.trade.adapter.web.fulfillment.res.OrderSummaryRes;
import com.lemnt.trade.adapter.web.fulfillment.res.RefundRequestRes;
import com.lemnt.trade.adapter.web.fulfillment.res.ShipmentRes;
import com.lemnt.trade.adapter.web.fulfillment.res.TradeOrderRefundRequestRefundRes;
import com.lemnt.trade.adapter.web.fulfillment.res.TradeRefundRequestDetailRes;
import com.lemnt.trade.adapter.web.fulfillment.res.TradeRefundRequestRejectRes;
import com.lemnt.trade.adapter.web.fulfillment.res.TradeShipmentReceiveRes;
import com.lemnt.trade.adapter.web.fulfillment.res.TradeShipmentShipRes;
import com.lemnt.trade.application.fulfillment.dto.OrderDetailDTO;
import com.lemnt.trade.application.fulfillment.dto.OrderSummaryDTO;
import com.lemnt.trade.application.fulfillment.dto.RefundRequestDTO;
import com.lemnt.trade.application.fulfillment.dto.ShipmentDTO;

/**
 * 履约响应转换器。
 */
public final class FulfillmentResConverter {
    private FulfillmentResConverter() {}

    /**
     * 发货。
     */
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

    /**
     * 退款。
     */
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

    /**
     * 汇总。
     */
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

    /**
     * 详情。
     */
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
                v.items().stream().map(item -> new com.lemnt.trade.adapter.web.fulfillment.res.OrderItemRes(item.productId(),
                item.productCode(),
                item.productName(),
                item.unitPrice(),
                item.quantity(),
                item.lineAmount(),
                item.spuId(),
                item.spuName(),
                item.properties() == null ? null : item.properties().stream().map(snapshotProperty ->
                        new com.lemnt.trade.adapter.web.fulfillment.res.OrderItemRes.Property(snapshotProperty.propertyId(), snapshotProperty.propertyName(),
                                snapshotProperty.valueId(), snapshotProperty.valueName())).toList(),
                item.picUrl())).toList(),
                shipment(v.shipment()));
    }

    /**
     * 退款。
     */
    public static TradeRefundRequestDetailRes refundToTradeRefundRequestDetailRes(RefundRequestDTO v) {
        return v == null
                ? null
                : new TradeRefundRequestDetailRes(
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

    /**
     * 退款。
     */
    public static TradeRefundRequestRejectRes refundToTradeRefundRequestRejectRes(RefundRequestDTO v) {
        return v == null
                ? null
                : new TradeRefundRequestRejectRes(
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

    /**
     * 退款。
     */
    public static TradeOrderRefundRequestRefundRes refundToTradeOrderRefundRequestRefundRes(RefundRequestDTO v) {
        return v == null
                ? null
                : new TradeOrderRefundRequestRefundRes(
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

    /**
     * 发货。
     */
    public static TradeShipmentShipRes shipmentToTradeShipmentShipRes(ShipmentDTO v) {
        return v == null
                ? null
                : new TradeShipmentShipRes(
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

    /**
     * 发货。
     */
    public static TradeShipmentReceiveRes shipmentToTradeShipmentReceiveRes(ShipmentDTO v) {
        return v == null
                ? null
                : new TradeShipmentReceiveRes(
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
}

package com.lemnt.trade.infrastructure.fulfillment.converter;

import com.lemnt.trade.domain.fulfillment.model.*;
import com.lemnt.trade.infrastructure.fulfillment.dataobject.*;

public final class FulfillmentConverter {
    private FulfillmentConverter() {}

    public static Shipment shipment(ShipmentDO row) {
        return row == null
                ? null
                : new Shipment(
                        row.getOrderNo(),
                        row.getRequestNo(),
                        row.getFingerprint(),
                        row.getCarrierCode(),
                        row.getTrackingNo(),
                        row.getRecipientName(),
                        row.getPhone(),
                        row.getAddress(),
                        row.getShippedAt(),
                        row.getReceivedAt());
    }

    public static ShipmentDO shipment(Shipment value) {
        var row = new ShipmentDO();
        row.setOrderNo(value.orderNo());
        row.setRequestNo(value.requestNo());
        row.setFingerprint(value.fingerprint());
        row.setCarrierCode(value.carrierCode());
        row.setTrackingNo(value.trackingNo());
        row.setRecipientName(value.recipientName());
        row.setPhone(value.phone());
        row.setAddress(value.address());
        row.setShippedAt(value.shippedAt());
        row.setReceivedAt(value.receivedAt());
        return row;
    }

    public static RefundRequest refundRequest(RefundRequestDO row) {
        return row == null
                ? null
                : new RefundRequest(
                        row.getRefundNo(),
                        row.getOrderNo(),
                        row.getRequestNo(),
                        row.getMemberId(),
                        row.getPaymentNo(),
                        row.getAmount(),
                        row.getReason(),
                        row.getStatus(),
                        row.getDecisionReason(),
                        row.getRequestedAt(),
                        row.getDecidedAt());
    }

    public static RefundRequestDO refundRequest(RefundRequest value) {
        var row = new RefundRequestDO();
        row.setRefundNo(value.refundNo());
        row.setOrderNo(value.orderNo());
        row.setRequestNo(value.requestNo());
        row.setMemberId(value.memberId());
        row.setPaymentNo(value.paymentNo());
        row.setAmount(value.amount());
        row.setReason(value.reason());
        row.setStatus(value.status());
        row.setDecisionReason(value.decisionReason());
        row.setRequestedAt(value.requestedAt());
        row.setDecidedAt(value.decidedAt());
        return row;
    }

    public static OrderSummary orderSummary(OrderSummaryDO row) {
        return row == null
                ? null
                : new OrderSummary(
                        row.getOrderNo(),
                        row.getMemberId(),
                        row.getTotalAmount(),
                        row.getStatus(),
                        row.getCreateTime(),
                        Boolean.TRUE.equals(row.getInventoryConfirmed()),
                        Boolean.TRUE.equals(row.getActiveRefund()),
                        row.getCarrierCode(),
                        row.getTrackingNo(),
                        row.getShippedAt(),
                        row.getReceivedAt());
    }
}

package com.lemnt.trade.infrastructure.fulfillment.converter;

import com.lemnt.trade.domain.fulfillment.model.OrderSummary;
import com.lemnt.trade.domain.fulfillment.model.RefundRequest;
import com.lemnt.trade.domain.fulfillment.model.Shipment;
import com.lemnt.trade.infrastructure.fulfillment.dataobject.OrderSummaryDO;
import com.lemnt.trade.infrastructure.aftersale.dataobject.AfterSaleDO;
import com.lemnt.trade.infrastructure.fulfillment.dataobject.ShipmentDO;

/**
 * 履约转换器。
 */
public final class FulfillmentConverter {
    private FulfillmentConverter() {}

    /**
     * 发货。
     */
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

    /**
     * 发货。
     */
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

    /**
     * 退款请求。
     */
    public static RefundRequest refundRequest(AfterSaleDO row) {
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

    /**
     * 退款请求。
     */
    public static AfterSaleDO refundRequest(RefundRequest value) {
        var row = new AfterSaleDO();
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

    /**
     * 订单汇总。
     */
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

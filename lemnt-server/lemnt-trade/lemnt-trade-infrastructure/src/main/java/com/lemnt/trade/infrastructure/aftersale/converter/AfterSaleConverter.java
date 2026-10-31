package com.lemnt.trade.infrastructure.aftersale.converter;



import com.alibaba.fastjson2.JSON;

import com.lemnt.trade.domain.aftersale.model.AfterSale;
import com.lemnt.trade.infrastructure.aftersale.dataobject.AfterSaleDO;
import com.lemnt.trade.infrastructure.order.converter.OrderItemConverter;

/** 售后表持久化转换。 */
public final class AfterSaleConverter {
    private AfterSaleConverter() {
    }

    /** 转换完整售后信息。 */
    public static AfterSale toModel(AfterSaleDO value) {
        if (value == null) {
            return null;
        }
        var properties = OrderItemConverter.properties(value.getProperties());
        return new AfterSale(value.getRefundNo(), value.getOrderNo(), value.getRequestNo(),
                value.getMemberId(), value.getPaymentNo(), value.getOrderItemId(), value.getQuantity(), value.getType(),
                value.getAmount(), value.getReason(), value.getStatus(), value.getDecisionReason(),
                value.getReturnCarrierCode(), value.getReturnTrackingNo(), value.getRequestedAt(), value.getDecidedAt(),
                value.getReturnShippedAt(), value.getReturnReceivedAt(), value.getId(), value.getOrderId(),
                value.getRefundTime(), value.getAuditUserId(), value.getSkuId(), value.getSpuId(), value.getSpuName(),
                properties == null ? null : properties.stream().map(snapshotProperty ->
                        new AfterSale.Property(snapshotProperty.propertyId(), snapshotProperty.propertyName(),
                                snapshotProperty.valueId(), snapshotProperty.valueName())).toList(), value.getPicUrl());
    }

    /** 转换售后写入字段。 */
    public static AfterSaleDO toDO(AfterSale value) {
        return new AfterSaleDO()
                .setId(value.id())
                .setOrderId(value.orderId())
                .setRefundTime(value.refundTime())
                .setAuditUserId(value.auditUserId())
                .setSkuId(value.skuId())
                .setSpuId(value.spuId())
                .setSpuName(value.spuName())
                .setProperties(value.properties() == null ? null
                        : JSON.toJSONString(value.properties()))
                .setPicUrl(value.picUrl())
                .setRefundNo(value.refundNo())
                .setOrderNo(value.orderNo())
                .setRequestNo(value.requestNo()).setMemberId(value.memberId()).setPaymentNo(value.paymentNo())
                .setOrderItemId(value.orderItemId()).setQuantity(value.quantity()).setType(value.type())
                .setAmount(value.amount()).setReason(value.reason()).setStatus(value.status())
                .setDecisionReason(value.decisionReason()).setRequestedAt(value.requestedAt()).setDecidedAt(value.decidedAt())
                .setReturnCarrierCode(value.returnCarrierCode()).setReturnTrackingNo(value.returnTrackingNo())
                .setReturnShippedAt(value.returnShippedAt()).setReturnReceivedAt(value.returnReceivedAt());
    }
}

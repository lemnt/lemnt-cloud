package com.lemnt.trade.application.aftersale.converter;

import com.lemnt.trade.application.aftersale.dto.AfterSaleDTO;
import com.lemnt.trade.domain.aftersale.model.AfterSale;

/** 商品售后应用输出转换。 */
public final class AfterSaleDTOConverter {
    private AfterSaleDTOConverter() {
    }

    /** 转换服务端售后记录。 */
    public static AfterSaleDTO toDTO(AfterSale value) {
        return value == null ? null : new AfterSaleDTO(value.refundNo(),
                value.orderNo(),
                value.requestNo(),
                value.memberId(),
                value.paymentNo(),
                value.orderItemId(),
                value.quantity(),
                value.type(),
                value.amount(),
                value.reason(),
                value.status(),
                value.decisionReason(),
                value.returnCarrierCode(),
                value.returnTrackingNo(),
                value.requestedAt(),
                value.decidedAt(),
                value.returnShippedAt(),
                value.returnReceivedAt(),
                value.id(),
                value.orderId(),
                value.refundTime(),
                value.auditUserId(),
                value.skuId(),
                value.spuId(),
                value.spuName(),
                value.properties() == null ? null : value.properties().stream().map(snapshotProperty ->
                        new AfterSaleDTO.Property(snapshotProperty.propertyId(), snapshotProperty.propertyName(),
                                snapshotProperty.valueId(), snapshotProperty.valueName())).toList(),
                value.picUrl());
    }
}

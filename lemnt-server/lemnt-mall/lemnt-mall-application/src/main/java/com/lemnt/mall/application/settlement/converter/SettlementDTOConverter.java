package com.lemnt.mall.application.settlement.converter;

import com.lemnt.mall.application.settlement.dto.SettlementDeliveryDTO;
import com.lemnt.mall.application.settlement.dto.SettlementItemDTO;
import com.lemnt.mall.application.settlement.dto.SettlementQuoteDTO;
import com.lemnt.mall.application.settlement.dto.SettlementReceiptDTO;
import com.lemnt.mall.domain.settlement.model.SettlementDelivery;
import com.lemnt.mall.domain.settlement.model.SettlementItem;
import com.lemnt.mall.domain.settlement.model.SettlementOrder;
import com.lemnt.mall.domain.settlement.model.SettlementQuote;
import com.lemnt.mall.domain.settlement.model.SettlementRecord;
import com.lemnt.mall.domain.settlement.model.SettlementRules;

/** 按业务域转换接口与应用数据。 */
public final class SettlementDTOConverter {
    private SettlementDTOConverter() {}

    /**
     * 明细。
     */
    public static SettlementItemDTO item(SettlementItem value) {
        return new SettlementItemDTO(value.productId(),
                value.productCode(),
                value.productName(),
                value.unitPrice(),
                value.quantity(),
                value.lineAmount(),
                value.payAmount(),
                value.deliveryPrice(),
                value.spuId(),
                value.spuName(),
                value.properties() == null ? null : value.properties().stream().map(snapshotProperty ->
                        new SettlementItemDTO.Property(snapshotProperty.propertyId(), snapshotProperty.propertyName(),
                                snapshotProperty.valueId(), snapshotProperty.valueName())).toList(),
                value.picUrl());
    }

    /**
     * 报价。
     */
    public static SettlementQuoteDTO quote(SettlementQuote value) {
        return new SettlementQuoteDTO(
                value.items().stream().map(SettlementDTOConverter::item).toList(),
                value.originalAmount(),
                value.discountAmount(),
                value.payableAmount(),
                value.couponId(),
                SettlementRules.quoteToken(value), value.usePoint(), value.givePoint(), delivery(value.delivery()));
    }

    /**
     * 回执。
     */
    public static SettlementReceiptDTO receipt(SettlementRecord value, SettlementOrder order) {
        return value == null || order == null
                ? null
                : new SettlementReceiptDTO(
                value.orderNo(),
                value.memberId(),
                order.status(),
                value.originalAmount(),
                value.discountAmount(),
                value.payableAmount(),
                value.couponId(),
                order.items().stream().map(SettlementDTOConverter::item).toList(), delivery(order.delivery()));
    }

    /** 转换服务端冻结的收件快照。 */
    public static SettlementDeliveryDTO delivery(
            SettlementDelivery value
    ) {
        return value == null ? null : new SettlementDeliveryDTO(
                value.addressId(), value.deliveryType(), value.receiverName(), value.receiverMobile(),
                value.receiverAreaId(), value.receiverDetailAddress(), value.deliveryPrice());
    }
}

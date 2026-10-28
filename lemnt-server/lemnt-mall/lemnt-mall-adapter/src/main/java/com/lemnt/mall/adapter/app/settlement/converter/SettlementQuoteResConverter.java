package com.lemnt.mall.adapter.app.settlement.converter;

import com.lemnt.mall.adapter.app.settlement.res.MallAppOrderOrderRes;
import com.lemnt.mall.adapter.app.settlement.res.MallAppOrderRecoverRes;
import com.lemnt.mall.adapter.app.settlement.res.SettlementDeliveryRes;
import com.lemnt.mall.adapter.app.settlement.res.SettlementItemRes;
import com.lemnt.mall.adapter.app.settlement.res.SettlementReceiptRes;
import com.lemnt.mall.adapter.app.settlement.res.SettlementSubmitRes;
import com.lemnt.mall.application.settlement.dto.SettlementDeliveryDTO;
import com.lemnt.mall.application.settlement.dto.SettlementItemDTO;
import com.lemnt.mall.application.settlement.dto.SettlementReceiptDTO;

/** 按业务域转换接口与应用数据。 */
public final class SettlementQuoteResConverter {
    private SettlementQuoteResConverter() {}

    /**
     * 结算明细。
     */
    public static SettlementItemRes checkoutItem(SettlementItemDTO value) {
        return value == null
                ? null
                : new SettlementItemRes(value.productId(),
                value.productCode(),
                value.productName(),
                value.unitPrice(),
                value.quantity(),
                value.lineAmount(),
                value.payAmount(),
                value.deliveryPrice(),
                value.spuId(),
                value.spuName(),
                value.properties(),
                value.picUrl());
    }

    /**
     * 结算回执。
     */
    public static SettlementReceiptRes checkoutReceipt(SettlementReceiptDTO value) {
        return value == null
                ? null
                : new SettlementReceiptRes(
                value.orderNo(),
                value.memberId(),
                value.status(),
                value.originalAmount(),
                value.discountAmount(),
                value.payableAmount(),
                value.couponId(),
                value.items().stream().map(SettlementQuoteResConverter::checkoutItem).toList(), delivery(value.delivery()));
    }

    /**
     * 结算回执。
     */
    public static SettlementSubmitRes submit(SettlementReceiptDTO value) {
        return value == null
                ? null
                : new SettlementSubmitRes(
                value.orderNo(),
                value.memberId(),
                value.status(),
                value.originalAmount(),
                value.discountAmount(),
                value.payableAmount(),
                value.couponId(),
                value.items().stream().map(SettlementQuoteResConverter::checkoutItem).toList(), delivery(value.delivery()));
    }

    /**
     * 结算回执。
     */
    public static MallAppOrderOrderRes checkoutReceiptToMallAppOrderOrderRes(SettlementReceiptDTO value) {
        return value == null
                ? null
                : new MallAppOrderOrderRes(
                value.orderNo(),
                value.memberId(),
                value.status(),
                value.originalAmount(),
                value.discountAmount(),
                value.payableAmount(),
                value.couponId(),
                value.items().stream().map(SettlementQuoteResConverter::checkoutItem).toList(), delivery(value.delivery()));
    }

    /**
     * 结算回执。
     */
    public static MallAppOrderRecoverRes checkoutReceiptToMallAppOrderRecoverRes(SettlementReceiptDTO value) {
        return value == null
                ? null
                : new MallAppOrderRecoverRes(
                value.orderNo(),
                value.memberId(),
                value.status(),
                value.originalAmount(),
                value.discountAmount(),
                value.payableAmount(),
                value.couponId(),
                value.items().stream().map(SettlementQuoteResConverter::checkoutItem).toList(), delivery(value.delivery()));
    }

    /** HTTP 只返回已经校验会员归属的收件快照。 */
    public static SettlementDeliveryRes delivery(
            SettlementDeliveryDTO value
    ) {
        return value == null ? null : new SettlementDeliveryRes(
                value.addressId(), value.deliveryType(), value.receiverName(), value.receiverMobile(),
                value.receiverAreaId(), value.receiverDetailAddress(), value.deliveryPrice());
    }
}

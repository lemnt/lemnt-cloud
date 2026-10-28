package com.lemnt.mall.adapter.web.settlement.converter;

import com.lemnt.mall.adapter.web.settlement.res.SettlementItemRes;
import com.lemnt.mall.adapter.web.settlement.res.SettlementQuoteRes;
import com.lemnt.mall.adapter.web.settlement.res.SettlementReceiptDetailRes;
import com.lemnt.mall.adapter.web.settlement.res.SettlementSubmitRes;
import com.lemnt.mall.application.settlement.dto.SettlementItemDTO;
import com.lemnt.mall.application.settlement.dto.SettlementQuoteDTO;
import com.lemnt.mall.application.settlement.dto.SettlementReceiptDTO;

/** 按业务域转换接口与应用数据。 */
public final class SettlementResConverter {
    private SettlementResConverter() {}

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
                value.spuId(),
                value.spuName(),
                value.properties(),
                value.picUrl());
    }

    /**
     * 结算报价。
     */
    public static SettlementQuoteRes checkoutQuote(SettlementQuoteDTO value) {
        return value == null
                ? null
                : new SettlementQuoteRes(
                value.items().stream().map(SettlementResConverter::checkoutItem).toList(),
                value.originalAmount(),
                value.discountAmount(),
                value.payableAmount(),
                value.couponId(),
                value.quoteToken());
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
                value.items().stream().map(SettlementResConverter::checkoutItem).toList());
    }

    /**
     * 结算回执。
     */
    public static SettlementReceiptDetailRes receiptDetail(SettlementReceiptDTO value) {
        return value == null
                ? null
                : new SettlementReceiptDetailRes(
                value.orderNo(),
                value.memberId(),
                value.status(),
                value.originalAmount(),
                value.discountAmount(),
                value.payableAmount(),
                value.couponId(),
                value.items().stream().map(SettlementResConverter::checkoutItem).toList());
    }
}

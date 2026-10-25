package com.lemnt.trade.adapter.web.checkout.converter;

import com.lemnt.trade.adapter.web.checkout.res.*;
import com.lemnt.trade.application.checkout.dto.*;

public final class CheckoutResConverter {
    private CheckoutResConverter() {}

    public static CartItemRes cartItem(CartItemDTO value) {
        return value == null
                ? null
                : new CartItemRes(
                        value.id(),
                        value.productId(),
                        value.quantity(),
                        value.selected(),
                        value.version(),
                        value.name(),
                        value.productCode(),
                        value.unitPrice(),
                        value.availableStock(),
                        value.sellable());
    }

    public static CouponRes coupon(CouponDTO value) {
        return value == null
                ? null
                : new CouponRes(
                        value.id(),
                        value.templateId(),
                        value.memberId(),
                        value.status(),
                        value.orderNo());
    }

    public static CouponTemplateRes couponTemplate(CouponTemplateDTO value) {
        return value == null
                ? null
                : new CouponTemplateRes(
                        value.id(),
                        value.code(),
                        value.name(),
                        value.kind(),
                        value.threshold(),
                        value.fixedAmount(),
                        value.discountPercent(),
                        value.discountLimit(),
                        value.totalCount(),
                        value.issuedCount(),
                        value.validFrom(),
                        value.validUntil(),
                        value.enabled());
    }

    public static CheckoutItemRes checkoutItem(CheckoutItemDTO value) {
        return value == null
                ? null
                : new CheckoutItemRes(
                        value.productId(),
                        value.productCode(),
                        value.productName(),
                        value.unitPrice(),
                        value.quantity(),
                        value.lineAmount());
    }

    public static CheckoutQuoteRes checkoutQuote(CheckoutQuoteDTO value) {
        return value == null
                ? null
                : new CheckoutQuoteRes(
                        value.items().stream().map(CheckoutResConverter::checkoutItem).toList(),
                        value.originalAmount(),
                        value.discountAmount(),
                        value.payableAmount(),
                        value.couponId(),
                        value.quoteToken());
    }

    public static CheckoutReceiptRes checkoutReceipt(CheckoutReceiptDTO value) {
        return value == null
                ? null
                : new CheckoutReceiptRes(
                        value.orderNo(),
                        value.memberId(),
                        value.status(),
                        value.originalAmount(),
                        value.discountAmount(),
                        value.payableAmount(),
                        value.couponId(),
                        value.items().stream().map(CheckoutResConverter::checkoutItem).toList());
    }
}

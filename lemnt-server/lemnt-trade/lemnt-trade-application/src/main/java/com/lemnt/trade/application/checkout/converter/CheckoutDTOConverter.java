package com.lemnt.trade.application.checkout.converter;

import com.lemnt.trade.application.checkout.dto.*;
import com.lemnt.trade.domain.checkout.model.*;
import com.lemnt.trade.domain.model.*;

public final class CheckoutDTOConverter {
    private CheckoutDTOConverter() {}

    public static CheckoutItemDTO item(TradeOrderItem value) {
        return new CheckoutItemDTO(
                value.productId(),
                value.productCode(),
                value.productName(),
                value.unitPrice(),
                value.quantity(),
                value.lineAmount());
    }

    public static CheckoutQuoteDTO quote(CheckoutQuote value) {
        return new CheckoutQuoteDTO(
                value.items().stream().map(CheckoutDTOConverter::item).toList(),
                value.originalAmount(),
                value.discountAmount(),
                value.payableAmount(),
                value.couponId(),
                CheckoutRules.quoteToken(value));
    }

    public static CheckoutReceiptDTO receipt(CheckoutRecord value, TradeOrder order) {
        return value == null || order == null
                ? null
                : new CheckoutReceiptDTO(
                        value.orderNo(),
                        value.memberId(),
                        order.status(),
                        value.originalAmount(),
                        value.discountAmount(),
                        value.payableAmount(),
                        value.couponId(),
                        order.items().stream().map(CheckoutDTOConverter::item).toList());
    }

    public static CouponDTO coupon(Coupon value) {
        return new CouponDTO(
                value.id(), value.templateId(), value.memberId(), value.status(), value.orderNo());
    }

    public static CouponTemplateDTO template(CouponTemplate value) {
        return new CouponTemplateDTO(
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

    public static CartItemDTO cart(CartLine value, ProductSnapshot product) {
        return new CartItemDTO(
                value.id(),
                value.productId(),
                value.quantity(),
                value.selected(),
                value.version(),
                product == null ? null : product.name(),
                product == null ? null : product.productCode(),
                product == null ? null : product.price(),
                product == null ? null : product.stock(),
                product != null);
    }
}

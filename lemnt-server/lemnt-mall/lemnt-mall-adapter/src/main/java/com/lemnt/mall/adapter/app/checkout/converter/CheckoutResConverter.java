package com.lemnt.mall.adapter.app.checkout.converter;

import com.lemnt.mall.adapter.app.checkout.res.CheckoutItemRes;
import com.lemnt.mall.adapter.app.checkout.res.CheckoutReceiptRes;
import com.lemnt.mall.adapter.app.checkout.res.CouponTemplateRes;
import com.lemnt.mall.adapter.app.checkout.res.MallAppCartCartRes;
import com.lemnt.mall.adapter.app.checkout.res.MallAppCartSaveRes;
import com.lemnt.mall.adapter.app.checkout.res.MallAppCheckoutCheckoutRes;
import com.lemnt.mall.adapter.app.checkout.res.MallAppCouponCenterClaimRes;
import com.lemnt.mall.adapter.app.checkout.res.MallAppCouponCouponsRes;
import com.lemnt.mall.adapter.app.checkout.res.MallAppOrderOrderRes;
import com.lemnt.mall.adapter.app.checkout.res.MallAppOrderRecoverRes;
import com.lemnt.mall.application.checkout.dto.CartItemDTO;
import com.lemnt.mall.application.checkout.dto.CheckoutItemDTO;
import com.lemnt.mall.application.checkout.dto.CheckoutReceiptDTO;
import com.lemnt.mall.application.checkout.dto.CouponDTO;
import com.lemnt.mall.application.checkout.dto.CouponTemplateDTO;

/**
 * 结算响应转换器。
 */
public final class CheckoutResConverter {
    private CheckoutResConverter() {}

    /**
     * 优惠券模板。
     */
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

    /**
     * 结算明细。
     */
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

    /**
     * 结算回执。
     */
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

    /**
     * 优惠券。
     */
    public static MallAppCouponCenterClaimRes couponToMallAppCouponCenterClaimRes(CouponDTO value) {
        return value == null
                ? null
                : new MallAppCouponCenterClaimRes(
                value.id(),
                value.templateId(),
                value.memberId(),
                value.status(),
                value.orderNo());
    }

    /**
     * 优惠券。
     */
    public static MallAppCouponCouponsRes couponToMallAppCouponCouponsRes(CouponDTO value) {
        return value == null
                ? null
                : new MallAppCouponCouponsRes(
                value.id(),
                value.templateId(),
                value.memberId(),
                value.status(),
                value.orderNo());
    }

    /**
     * 购物车明细。
     */
    public static MallAppCartCartRes cartItemToMallAppCartCartRes(CartItemDTO value) {
        return value == null
                ? null
                : new MallAppCartCartRes(
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

    /**
     * 购物车明细。
     */
    public static MallAppCartSaveRes cartItemToMallAppCartSaveRes(CartItemDTO value) {
        return value == null
                ? null
                : new MallAppCartSaveRes(
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

    /**
     * 结算回执。
     */
    public static MallAppCheckoutCheckoutRes checkoutReceiptToMallAppCheckoutCheckoutRes(CheckoutReceiptDTO value) {
        return value == null
                ? null
                : new MallAppCheckoutCheckoutRes(
                value.orderNo(),
                value.memberId(),
                value.status(),
                value.originalAmount(),
                value.discountAmount(),
                value.payableAmount(),
                value.couponId(),
                value.items().stream().map(CheckoutResConverter::checkoutItem).toList());
    }

    /**
     * 结算回执。
     */
    public static MallAppOrderOrderRes checkoutReceiptToMallAppOrderOrderRes(CheckoutReceiptDTO value) {
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
                value.items().stream().map(CheckoutResConverter::checkoutItem).toList());
    }

    /**
     * 结算回执。
     */
    public static MallAppOrderRecoverRes checkoutReceiptToMallAppOrderRecoverRes(CheckoutReceiptDTO value) {
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
                value.items().stream().map(CheckoutResConverter::checkoutItem).toList());
    }
}

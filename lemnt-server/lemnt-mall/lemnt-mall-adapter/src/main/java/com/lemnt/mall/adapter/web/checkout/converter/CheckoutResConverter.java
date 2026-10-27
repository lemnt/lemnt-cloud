package com.lemnt.mall.adapter.web.checkout.converter;

import com.lemnt.mall.adapter.web.checkout.res.CheckoutItemRes;
import com.lemnt.mall.adapter.web.checkout.res.CheckoutQuoteRes;
import com.lemnt.mall.adapter.web.checkout.res.MallCartListRes;
import com.lemnt.mall.adapter.web.checkout.res.MallCartSaveRes;
import com.lemnt.mall.adapter.web.checkout.res.MallCheckoutCheckoutRes;
import com.lemnt.mall.adapter.web.checkout.res.MallCheckoutReceiptRes;
import com.lemnt.mall.adapter.web.checkout.res.MallCouponCouponsRes;
import com.lemnt.mall.adapter.web.checkout.res.MallCouponIssueRes;
import com.lemnt.mall.adapter.web.checkout.res.MallCouponTemplateCreateRes;
import com.lemnt.mall.adapter.web.checkout.res.MallCouponTemplateTemplatesRes;
import com.lemnt.mall.application.checkout.dto.CartItemDTO;
import com.lemnt.mall.application.checkout.dto.CheckoutItemDTO;
import com.lemnt.mall.application.checkout.dto.CheckoutQuoteDTO;
import com.lemnt.mall.application.checkout.dto.CheckoutReceiptDTO;
import com.lemnt.mall.application.checkout.dto.CouponDTO;
import com.lemnt.mall.application.checkout.dto.CouponTemplateDTO;

/**
 * 结算响应转换器。
 */
public final class CheckoutResConverter {
    private CheckoutResConverter() {}

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
     * 结算报价。
     */
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

    /**
     * 优惠券模板。
     */
    public static MallCouponTemplateTemplatesRes couponTemplateToMallCouponTemplateTemplatesRes(CouponTemplateDTO value) {
        return value == null
                ? null
                : new MallCouponTemplateTemplatesRes(
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
     * 优惠券模板。
     */
    public static MallCouponTemplateCreateRes couponTemplateToMallCouponTemplateCreateRes(CouponTemplateDTO value) {
        return value == null
                ? null
                : new MallCouponTemplateCreateRes(
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
     * 结算回执。
     */
    public static MallCheckoutCheckoutRes checkoutReceiptToMallCheckoutCheckoutRes(CheckoutReceiptDTO value) {
        return value == null
                ? null
                : new MallCheckoutCheckoutRes(
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
    public static MallCheckoutReceiptRes checkoutReceiptToMallCheckoutReceiptRes(CheckoutReceiptDTO value) {
        return value == null
                ? null
                : new MallCheckoutReceiptRes(
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
    public static MallCouponIssueRes couponToMallCouponIssueRes(CouponDTO value) {
        return value == null
                ? null
                : new MallCouponIssueRes(
                value.id(),
                value.templateId(),
                value.memberId(),
                value.status(),
                value.orderNo());
    }

    /**
     * 优惠券。
     */
    public static MallCouponCouponsRes couponToMallCouponCouponsRes(CouponDTO value) {
        return value == null
                ? null
                : new MallCouponCouponsRes(
                value.id(),
                value.templateId(),
                value.memberId(),
                value.status(),
                value.orderNo());
    }

    /**
     * 购物车明细。
     */
    public static MallCartListRes cartItemToMallCartListRes(CartItemDTO value) {
        return value == null
                ? null
                : new MallCartListRes(
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
    public static MallCartSaveRes cartItemToMallCartSaveRes(CartItemDTO value) {
        return value == null
                ? null
                : new MallCartSaveRes(
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
}

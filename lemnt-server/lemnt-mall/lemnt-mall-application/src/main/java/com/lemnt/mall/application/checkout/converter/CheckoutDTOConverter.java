package com.lemnt.mall.application.checkout.converter;

import com.lemnt.mall.application.checkout.dto.CartItemDTO;
import com.lemnt.mall.application.checkout.dto.CheckoutItemDTO;
import com.lemnt.mall.application.checkout.dto.CheckoutQuoteDTO;
import com.lemnt.mall.application.checkout.dto.CheckoutReceiptDTO;
import com.lemnt.mall.application.checkout.dto.CouponDTO;
import com.lemnt.mall.application.checkout.dto.CouponTemplateDTO;
import com.lemnt.mall.domain.catalog.model.ProductSnapshot;
import com.lemnt.mall.domain.checkout.model.CartLine;
import com.lemnt.mall.domain.checkout.model.CheckoutItem;
import com.lemnt.mall.domain.checkout.model.CheckoutQuote;
import com.lemnt.mall.domain.checkout.model.CheckoutRecord;
import com.lemnt.mall.domain.checkout.model.CheckoutRules;
import com.lemnt.mall.domain.checkout.model.Coupon;
import com.lemnt.mall.domain.checkout.model.CouponTemplate;
import com.lemnt.mall.domain.order.model.OrderSnapshot;

/**
 * 结算数据传输对象转换器。
 */
public final class CheckoutDTOConverter {
    private CheckoutDTOConverter() {}

    /**
     * 明细。
     */
    public static CheckoutItemDTO item(CheckoutItem value) {
        return new CheckoutItemDTO(
                value.productId(),
                value.productCode(),
                value.productName(),
                value.unitPrice(),
                value.quantity(),
                value.lineAmount());
    }

    /**
     * 报价。
     */
    public static CheckoutQuoteDTO quote(CheckoutQuote value) {
        return new CheckoutQuoteDTO(
                value.items().stream().map(CheckoutDTOConverter::item).toList(),
                value.originalAmount(),
                value.discountAmount(),
                value.payableAmount(),
                value.couponId(),
                CheckoutRules.quoteToken(value));
    }

    /**
     * 回执。
     */
    public static CheckoutReceiptDTO receipt(CheckoutRecord value, OrderSnapshot order) {
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

    /**
     * 优惠券。
     */
    public static CouponDTO coupon(Coupon value) {
        return new CouponDTO(
                value.id(), value.templateId(), value.memberId(), value.status(), value.orderNo());
    }

    /**
     * 模板。
     */
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

    /**
     * 购物车。
     */
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

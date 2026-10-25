package com.lemnt.trade.infrastructure.checkout.converter;

import com.lemnt.trade.domain.checkout.model.*;
import com.lemnt.trade.infrastructure.checkout.dataobject.*;

public final class CheckoutConverter {
    private CheckoutConverter() {}

    public static CartLine cart(CartDO row) {
        return row == null
                ? null
                : new CartLine(
                        row.getId(),
                        row.getMemberId(),
                        row.getProductId(),
                        row.getQuantity(),
                        Boolean.TRUE.equals(row.getSelected()),
                        row.getVersion());
    }

    public static CouponTemplate template(CouponTemplateDO row) {
        return row == null
                ? null
                : new CouponTemplate(
                        row.getId(),
                        row.getCode(),
                        row.getName(),
                        row.getKind(),
                        row.getThreshold(),
                        row.getFixedAmount(),
                        row.getDiscountPercent(),
                        row.getDiscountLimit(),
                        row.getTotalCount(),
                        row.getIssuedCount(),
                        row.getValidFrom(),
                        row.getValidUntil(),
                        Boolean.TRUE.equals(row.getEnabled()));
    }

    public static Coupon coupon(CouponDO row) {
        return row == null
                ? null
                : new Coupon(
                        row.getId(),
                        row.getTemplateId(),
                        row.getMemberId(),
                        row.getStatus(),
                        row.getOrderNo());
    }

    public static CheckoutRecord checkout(CheckoutDO row, java.util.List<CheckoutCartDO> refs) {
        return row == null
                ? null
                : new CheckoutRecord(
                        row.getOrderNo(),
                        row.getMemberId(),
                        row.getRequestNo(),
                        row.getFingerprint(),
                        row.getOriginalAmount(),
                        row.getDiscountAmount(),
                        row.getPayableAmount(),
                        row.getCouponId(),
                        refs.stream()
                                .map(r -> new CartReference(r.getCartId(), r.getCartVersion()))
                                .toList());
    }
}

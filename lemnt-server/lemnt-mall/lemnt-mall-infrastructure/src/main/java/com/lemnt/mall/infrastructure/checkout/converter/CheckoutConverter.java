package com.lemnt.mall.infrastructure.checkout.converter;

import com.lemnt.mall.domain.checkout.model.CartLine;
import com.lemnt.mall.domain.checkout.model.CartReference;
import com.lemnt.mall.domain.checkout.model.CheckoutRecord;
import com.lemnt.mall.domain.checkout.model.Coupon;
import com.lemnt.mall.domain.checkout.model.CouponTemplate;
import com.lemnt.mall.infrastructure.checkout.dataobject.CartDO;
import com.lemnt.mall.infrastructure.checkout.dataobject.CheckoutCartDO;
import com.lemnt.mall.infrastructure.checkout.dataobject.CheckoutDO;
import com.lemnt.mall.infrastructure.checkout.dataobject.CouponDO;
import com.lemnt.mall.infrastructure.checkout.dataobject.CouponTemplateDO;

import java.util.List;

/**
 * 结算转换器。
 */
public final class CheckoutConverter {
    private CheckoutConverter() {}

    /**
     * 购物车。
     */
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

    /**
     * 模板。
     */
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

    /**
     * 优惠券。
     */
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

    /**
     * 结算。
     */
    public static CheckoutRecord checkout(CheckoutDO row, List<CheckoutCartDO> refs) {
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

package com.lemnt.trade.domain.promotion.model;

import com.lemnt.trade.domain.checkout.model.Coupon;
import com.lemnt.trade.domain.checkout.model.CouponTemplate;
import java.time.LocalDateTime;

/** Selected, eligible coupon snapshot. Never selects a coupon on behalf of the member. */
public record SelectedCoupon(long id, DiscountRule rule) {
    public SelectedCoupon {
        PricingException.require(id > 0);
        PricingException.require(
                rule instanceof DiscountRule.Fixed || rule instanceof DiscountRule.Percentage);
        if (rule instanceof DiscountRule.Percentage percent)
            PricingException.require(percent.payRateBps() % 100 == 0);
    }

    /** Both times and records must be supplied by the authoritative server-side gateway. */
    public static SelectedCoupon fromLegacy(
            long memberId, Coupon coupon, CouponTemplate template, LocalDateTime databaseLocalNow) {
        if (memberId <= 0
                || coupon == null
                || template == null
                || databaseLocalNow == null
                || coupon.id() == null
                || coupon.id() <= 0
                || coupon.memberId() == null
                || coupon.memberId() != memberId
                || !"AVAILABLE".equals(coupon.status())
                || coupon.orderNo() != null
                || template.id() == null
                || !template.id().equals(coupon.templateId())
                || !template.enabled()
                || template.validFrom() == null
                || template.validUntil() == null
                || !template.validUntil().isAfter(template.validFrom())
                || databaseLocalNow.isBefore(template.validFrom())
                || !databaseLocalNow.isBefore(template.validUntil())) throw ineligible();
        long threshold = FenAmounts.fromYuan(template.threshold());
        DiscountRule rule;
        if ("FIXED".equals(template.kind())) {
            PricingException.require(
                    template.discountPercent() == null && template.discountLimit() == null);
            rule = new DiscountRule.Fixed(threshold, FenAmounts.fromYuan(template.fixedAmount()));
        } else if ("PERCENT".equals(template.kind())) {
            PricingException.require(
                    template.fixedAmount() == null
                            && template.discountPercent() != null
                            && template.discountPercent() >= 1
                            && template.discountPercent() <= 99);
            rule =
                    new DiscountRule.Percentage(
                            threshold,
                            template.discountPercent() * 100,
                            template.discountLimit() == null
                                    ? null
                                    : FenAmounts.fromYuan(template.discountLimit()));
        } else {
            throw new PricingException(PricingException.Code.INVALID_PROMOTION_REQUEST);
        }
        return new SelectedCoupon(coupon.id(), rule);
    }

    private static PricingException ineligible() {
        return new PricingException(PricingException.Code.COUPON_NOT_ELIGIBLE);
    }
}

package com.lemnt.trade.domain.checkout.model;

import com.lemnt.trade.domain.model.TradeOrderItem;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.*;

public final class CheckoutRules {
    private CheckoutRules() {}

    public static void id(Long value) {
        if (value == null || value <= 0) throw new IllegalArgumentException("编号不合法");
    }

    public static String text(String value, int maximum) {
        if (value == null || value.isBlank() || value.trim().length() > maximum)
            throw new IllegalArgumentException("文本参数不合法");
        return value.trim();
    }

    public static BigDecimal money(BigDecimal value, boolean allowZero) {
        if (value == null
                || value.signum() < 0
                || (!allowZero && value.signum() == 0)
                || value.precision() - value.scale() > 17) {
            throw new IllegalArgumentException("金额不合法");
        }
        try {
            return value.setScale(2, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException ex) {
            throw new IllegalArgumentException("金额最多两位小数", ex);
        }
    }

    public static CouponTemplate template(CouponTemplate value) {
        if (value == null
                || value.validFrom() == null
                || value.validUntil() == null
                || !value.validUntil().isAfter(value.validFrom())
                || value.totalCount() < 1
                || value.totalCount() > 1000000) throw new IllegalArgumentException("优惠券有效期或数量不合法");
        if (!Set.of("FIXED", "PERCENT").contains(value.kind() == null ? "" : value.kind()))
            throw new IllegalArgumentException("优惠券类型不合法");
        BigDecimal fixed = null;
        Integer percent = null;
        BigDecimal limit = null;
        if ("FIXED".equals(value.kind())) {
            fixed = money(value.fixedAmount(), false);
            if (value.discountPercent() != null || value.discountLimit() != null)
                throw new IllegalArgumentException("满减券不能填写折扣参数");
        } else {
            percent = value.discountPercent();
            if (percent == null || percent < 1 || percent > 99 || value.fixedAmount() != null)
                throw new IllegalArgumentException("折扣为1至99的支付百分比");
            if (value.discountLimit() != null) limit = money(value.discountLimit(), false);
        }
        return new CouponTemplate(
                null,
                text(value.code(), 64),
                text(value.name(), 100),
                value.kind(),
                money(value.threshold(), true),
                fixed,
                percent,
                limit,
                value.totalCount(),
                0,
                value.validFrom(),
                value.validUntil(),
                true);
    }

    public static BigDecimal discount(
            CouponTemplate template, BigDecimal gross, LocalDateTime now) {
        if (!template.enabled()
                || now.isBefore(template.validFrom())
                || !now.isBefore(template.validUntil())) {
            throw new IllegalArgumentException("优惠券未生效、已到期或模板已停用");
        }
        if (gross.compareTo(template.threshold()) < 0)
            throw new IllegalArgumentException("订单金额未达到用券门槛");
        BigDecimal amount =
                "FIXED".equals(template.kind())
                        ? template.fixedAmount()
                        : gross.multiply(BigDecimal.valueOf(100 - template.discountPercent()))
                                .divide(BigDecimal.valueOf(100), 2, RoundingMode.DOWN);
        if ("PERCENT".equals(template.kind()) && template.discountLimit() != null)
            amount = amount.min(template.discountLimit());
        return money(amount.min(gross), true);
    }

    public static CheckoutQuote quote(
            List<TradeOrderItem> items, Coupon coupon, CouponTemplate template, LocalDateTime now) {
        if (items == null || items.isEmpty() || items.size() > 100)
            throw new IllegalArgumentException("订单必须包含1至100项SKU");
        BigDecimal gross = BigDecimal.ZERO;
        for (var item : items) {
            id(item.productId());
            if (item.quantity() == null || item.quantity() < 1 || item.quantity() > 999)
                throw new IllegalArgumentException("购买数量不合法");
            BigDecimal line =
                    money(item.unitPrice(), false).multiply(BigDecimal.valueOf(item.quantity()));
            if (line.compareTo(item.lineAmount()) != 0)
                throw new IllegalArgumentException("订单行金额不匹配");
            gross = gross.add(line);
        }
        gross = money(gross, false);
        BigDecimal discount = new BigDecimal("0.00");
        if (coupon != null) {
            if (!"AVAILABLE".equals(coupon.status())
                    || template == null
                    || !coupon.templateId().equals(template.id()))
                throw new IllegalArgumentException("优惠券不可用");
            discount = discount(template, gross, now);
        }
        return new CheckoutQuote(
                items,
                gross,
                discount,
                gross.subtract(discount),
                coupon == null ? null : coupon.id());
    }

    public static String quoteToken(CheckoutQuote quote) {
        StringBuilder value = new StringBuilder();
        for (var item : quote.items())
            value.append(item.productId())
                    .append(':')
                    .append(item.quantity())
                    .append(':')
                    .append(item.unitPrice().toPlainString())
                    .append(':')
                    .append(item.productCode().length())
                    .append(':')
                    .append(item.productCode())
                    .append(item.productName().length())
                    .append(':')
                    .append(item.productName());
        value.append('|')
                .append(quote.couponId())
                .append('|')
                .append(quote.originalAmount())
                .append('|')
                .append(quote.discountAmount())
                .append('|')
                .append(quote.payableAmount());
        return fingerprint(value.toString());
    }

    public static String fingerprint(String canonical) {
        try {
            return HexFormat.of()
                    .formatHex(
                            MessageDigest.getInstance("SHA-256")
                                    .digest(canonical.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }
}

package com.lemnt.mall.domain.coupon.model;

import com.lemnt.common.core.exception.ServiceException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Set;

/** 优惠券模板与折扣计算规则。 */
public final class CouponRules {
    private CouponRules() {}

    /**
     * 模板。
     */
    public static CouponTemplate template(CouponTemplate value) {
        if (value == null || (value.totalCount() != -1 && value.totalCount() < 1)
                || (value.takeLimitCount() != -1 && value.takeLimitCount() < 1)
                || !Set.of("USER", "ADMIN", "REGISTER").contains(value.takeType())
                || !Set.of("DATE", "TERM").contains(value.validityType())
                || !Set.of("ALL", "SPU_IDS", "CATEGORY_IDS").contains(value.productScope())) {
            throw new ServiceException("优惠券模板类型、数量或范围不合法", 400);
        }
        if ("DATE".equals(value.validityType()) && (value.validFrom() == null || value.validUntil() == null
                || !value.validUntil().isAfter(value.validFrom()))) {
            throw new ServiceException("固定日期有效期不合法", 400);
        }
        if ("TERM".equals(value.validityType()) && (value.fixedStartTerm() == null || value.fixedStartTerm() < 0
                || value.fixedEndTerm() == null || value.fixedEndTerm() <= 0)) {
            throw new ServiceException("领取后有效期不合法", 400);
        }
        if (value.productScopeValues().size() > 1000 || value.productScopeValues().stream()
                .anyMatch(id -> id == null || id <= 0)
                || value.productScopeValues().stream().distinct().count() != value.productScopeValues().size()
                || (!"ALL".equals(value.productScope()) && value.productScopeValues().isEmpty())) {
            throw new ServiceException("优惠券适用商品范围不合法", 400);
        }
        if ("USER".equals(value.takeType()) && value.totalCount() != -1 && value.takeLimitCount() != -1
                && value.takeLimitCount() > value.totalCount()) {
            throw new ServiceException("每人限领数不能超过发放总量", 400);
        }
        if (!Set.of("FIXED", "PERCENT").contains(value.kind() == null ? "" : value.kind())) {
            throw new IllegalArgumentException("优惠券类型不合法");
        }
        BigDecimal fixed = null;
        Integer percent = null;
        BigDecimal limit = null;
        if ("FIXED".equals(value.kind())) {
            fixed = money(value.fixedAmount(), false);
            if (value.discountPercent() != null || value.discountLimit() != null) {
                throw new IllegalArgumentException("满减券不能填写折扣参数");
            }
        } else {
            percent = value.discountPercent();
            if (percent == null || percent < 1 || percent > 100 || value.fixedAmount() != null) {
                throw new IllegalArgumentException("折扣为1至100的支付百分比");
            }
            if (value.discountLimit() != null) {
                limit = money(value.discountLimit(), false);
            }
        }
        return new CouponTemplate(null, text(value.code(), 64), text(value.name(), 100), value.kind(),
                money(value.threshold(), true), fixed, percent, limit, value.totalCount(), 0,
                "DATE".equals(value.validityType()) ? value.validFrom() : null,
                "DATE".equals(value.validityType()) ? value.validUntil() : null, true,
                value.description(), value.takeType(), value.takeLimitCount(), value.productScope(),
                value.productScopeValues(), value.validityType(), value.fixedStartTerm(), value.fixedEndTerm(), 0);
    }

    /** 领取时冻结规则，相对有效期从数据库当前时刻推算。 */
    public static CouponTerms snapshot(CouponTemplate value, LocalDateTime now) {
        LocalDateTime from = "TERM".equals(value.validityType()) ? now.plusDays(value.fixedStartTerm()) : value.validFrom();
        LocalDateTime until = "TERM".equals(value.validityType()) ? from.plusDays(value.fixedEndTerm()) : value.validUntil();
        return new CouponTerms(value.code(), value.name(), value.takeType(), value.kind(), value.threshold(),
                value.fixedAmount(), value.discountPercent(), value.discountLimit(), value.productScope(),
                value.productScopeValues(), from, until);
    }

    /** 已发券只根据自己的有效期、门槛和金额计算，不读取模板启停。 */
    public static BigDecimal discount(CouponTerms terms, BigDecimal gross, LocalDateTime now) {
        if (terms == null || now.isBefore(terms.validFrom()) || !now.isBefore(terms.validUntil())) {
            throw new ServiceException("优惠券未生效或已过期", 400);
        }
        if (gross.compareTo(terms.threshold()) < 0) {
            throw new ServiceException("适用商品金额未达到用券门槛", 400);
        }
        var amount = "FIXED".equals(terms.kind()) ? terms.fixedAmount()
                : gross.multiply(BigDecimal.valueOf(100 - terms.discountPercent()))
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.DOWN);
        if (terms.discountLimit() != null) {
            amount = amount.min(terms.discountLimit());
        }
        return money(amount.min(gross), true);
    }

    /**
     * 折扣。
     */
    public static BigDecimal discount(
            CouponTemplate template, BigDecimal gross, LocalDateTime now) {
        if (!template.enabled()
                || now.isBefore(template.validFrom())
                || !now.isBefore(template.validUntil())) {
            throw new IllegalArgumentException("优惠券未生效、已到期或模板已停用");
        }
        if (gross.compareTo(template.threshold()) < 0) {
            throw new IllegalArgumentException("订单金额未达到用券门槛");
        }
        BigDecimal amount =
                "FIXED".equals(template.kind())
                ? template.fixedAmount()
                : gross.multiply(BigDecimal.valueOf(100 - template.discountPercent()))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.DOWN);
        if ("PERCENT".equals(template.kind()) && template.discountLimit() != null) {
            amount = amount.min(template.discountLimit());
        }
        return money(amount.min(gross), true);
    }

    /** 校验业务编号。 */
    public static void id(Long value) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("编号不合法");
        }
    }

    /** 校验并规范业务文本。 */
    public static String text(String value, int maximum) {
        if (value == null || value.isBlank() || value.trim().length() > maximum) {
            throw new IllegalArgumentException("文本参数不合法");
        }
        return value.trim();
    }

    /** 校验并规范业务金额。 */
    public static BigDecimal money(BigDecimal value, boolean allowZero) {
        if (value == null
                || value.signum() < 0
                ||(!allowZero && value.signum() == 0)
                || value.precision() - value.scale() > 17) {
            throw new IllegalArgumentException("金额不合法");
        }
        try {
            return value.setScale(2, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException ex) {
            throw new IllegalArgumentException("金额最多两位小数", ex);
        }
    }
}

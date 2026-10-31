package com.lemnt.mall.domain.settlement.model;

import com.lemnt.mall.domain.coupon.model.Coupon;
import com.lemnt.mall.domain.coupon.model.CouponTemplate;

import static com.lemnt.mall.domain.coupon.model.CouponRules.discount;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;

/**
 * 结算规则。
 */
public final class SettlementRules {
    private SettlementRules() {}

    /**
     * 报价。
     */
    public static SettlementQuote quote(
            List<SettlementItem> items, Coupon coupon, CouponTemplate template, LocalDateTime now
    ) {
        if (items == null || items.isEmpty() || items.size() > 100
        ) {
            throw new IllegalArgumentException("订单必须包含1至100项SKU");
        }
        BigDecimal gross = BigDecimal.ZERO;
        for (var item : items
        ) {
            id(item.productId());
            if (item.quantity() == null || item.quantity() < 1 || item.quantity() > 999
            ) {
                throw new IllegalArgumentException("购买数量不合法");
            }
            BigDecimal line =
                    money(item.unitPrice(), false).multiply(BigDecimal.valueOf(item.quantity()));
            if (line.compareTo(item.lineAmount()) != 0
            ) {
                throw new IllegalArgumentException("订单行金额不匹配");
            }
            gross = gross.add(line);
        }
        gross = money(gross, false);
        BigDecimal discount = new BigDecimal("0.00");
        if (coupon != null
        ) {
            if (!"AVAILABLE".equals(coupon.status())
                    || (coupon.terms() == null && (template == null
                    || !coupon.templateId().equals(template.id())))
            ) {
                throw new IllegalArgumentException("优惠券不可用");
            }
            discount = coupon.terms() == null ? discount(template, gross, now) : discount(coupon.terms(), gross, now);
        }
        return new SettlementQuote(
                items,
                gross,
                discount,
                gross.subtract(discount),
                coupon == null ? null : coupon.id());
    }

    /**
     * 报价令牌。
     */
    public static String quoteToken(SettlementQuote quote) {
        StringBuilder value = new StringBuilder();
        for (var item : quote.items()
        ) {
            value.append(item.productId())
                    .append(':')
                    .append(item.quantity())
                    .append(':')
                    .append(item.unitPrice().toPlainString())
                    .append(':')
                    .append(item.payAmount()).append(':').append(item.deliveryPrice())
                    .append(':')
                    .append(item.productCode().length())
                    .append(':')
                    .append(item.productCode())
                    .append(item.productName().length())
                    .append(':')
                    .append(item.productName());
            value.append("|spu:").append(item.spuId());
            appendSnapshotText(value, item.spuName());
            appendSnapshotText(value, item.picUrl());
            if (item.properties() == null) {
                value.append("|properties:null");
            } else {
                value.append("|properties:").append(item.properties().size());
                item.properties().stream().sorted(java.util.Comparator
                        .comparing(SettlementItem.Property::propertyName)
                        .thenComparing(property -> property.propertyId(), java.util.Comparator.nullsFirst(Long::compareTo))
                        .thenComparing(SettlementItem.Property::valueName)).forEach(property -> {
                    value.append("|propertyId:").append(property.propertyId());
                    appendSnapshotText(value, property.propertyName());
                    value.append("|valueId:").append(property.valueId());
                    appendSnapshotText(value, property.valueName());
                });
            }
        }
        value.append('|')
                .append(quote.couponId())
                .append('|')
                .append(quote.originalAmount())
                .append('|')
                .append(quote.discountAmount())
                .append('|')
                .append(quote.payableAmount());
        var delivery = quote.delivery();
        if (delivery != null
        ) {
            value.append('|').append(delivery.addressId()).append('|').append(delivery.deliveryType())
                    .append('|').append(delivery.receiverAreaId()).append('|').append(delivery.deliveryPrice());
            for (var text : List.of(delivery.receiverName(), delivery.receiverMobile(),
                    delivery.receiverDetailAddress())
            ) {
                value.append('|').append(text.length()).append(':').append(text);
            }
        }
        return fingerprint(value.append('|').append(quote.usePoint()).append('|').append(quote.givePoint()).toString());
    }

    /** 校验业务编号。 */
    public static void id(Long value) {
        if (value == null || value <= 0
        ) {
            throw new IllegalArgumentException("编号不合法");
        }
    }

    /** 校验并规范业务文本。 */
    public static String text(String value, int maximum) {
        if (value == null || value.isBlank() || value.trim().length() > maximum
        ) {
            throw new IllegalArgumentException("文本参数不合法");
        }
        return value.trim();
    }

    /** 校验并规范业务金额。 */
    public static BigDecimal money(BigDecimal value, boolean allowZero) {
        if (value == null
                || value.signum() < 0
                ||(!allowZero && value.signum() == 0)
                || value.precision() - value.scale() > 17
        ) {
            throw new IllegalArgumentException("金额不合法");
        }
        try {
            return value.setScale(2, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException ex
        ) {
            throw new IllegalArgumentException("金额最多两位小数", ex);
        }
    }

    /** 计算业务请求内容指纹。 */
    public static String fingerprint(String canonical) {
        try {
            return HexFormat.of()
                    .formatHex(
                    MessageDigest.getInstance("SHA-256")
                    .digest(canonical.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex
        ) {
            throw new IllegalStateException(ex);
        }
    }
    /** 长度前缀区分空值和包含分隔符的规格/图片，排序后的Map不受迭代顺序影响。 */
    private static void appendSnapshotText(StringBuilder target, String value) {
        target.append('|').append(value == null ? -1 : value.length()).append(':');
        if (value != null) {
            target.append(value);
        }
    }
}

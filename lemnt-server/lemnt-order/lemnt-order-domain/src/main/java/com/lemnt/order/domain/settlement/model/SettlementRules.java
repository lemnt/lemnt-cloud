package com.lemnt.order.domain.settlement.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.Map;

/**
 * 结算规则。
 */
public final class SettlementRules {
    private SettlementRules() {}

    /**
     * 报价令牌。
     */
    public static String quoteToken(SettlementQuote quote) {
        StringBuilder value = new StringBuilder();
        for (var item : quote.items()) {
            value.append(item.productId())
                    .append(':')
                    .append(item.quantity())
                    .append(':')
                    .append(item.unitPrice().toPlainString())
                    .append(':')
                    .append(item.payAmount())
                    .append(':')
                    .append(item.deliveryPrice())
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
            if (item.specs() == null) {
                value.append("|specs:null");
            } else {
                value.append("|specs:").append(item.specs().size());
                item.specs().stream()
                        .sorted(
                                Comparator.comparing(SettlementItem.Spec::specName)
                                        .thenComparing(
                                                property -> property.specId(),
                                                Comparator.nullsFirst(Long::compareTo))
                                        .thenComparing(SettlementItem.Spec::specValueName))
                        .forEach(
                                property -> {
                                    value.append("|specId:").append(property.specId());
                                    appendSnapshotText(value, property.specName());
                                    value.append("|specValueId:").append(property.specValueId());
                                    appendSnapshotText(value, property.specValueName());
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
        if (delivery != null) {
            value.append('|')
                    .append(delivery.addressId())
                    .append('|')
                    .append(delivery.deliveryType())
                    .append('|')
                    .append(delivery.receiverAreaId())
                    .append('|')
                    .append(delivery.deliveryPrice())
                    .append('|')
                    .append(delivery.pickUpStoreId());
            for (var text :
                    Arrays.asList(
                            delivery.receiverName(),
                            delivery.receiverMobile(),
                            delivery.receiverDetailAddress())) {
                value.append('|')
                        .append(text == null ? -1 : text.length())
                        .append(':')
                        .append(text);
            }
        }
        value.append("|reward:")
                .append(quote.rewardPoint())
                .append('|')
                .append(quote.freeDelivery());
        if (quote.giveCouponTemplateCounts() != null) {
            quote.giveCouponTemplateCounts().entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(
                            entry ->
                                    value.append("|gift:")
                                            .append(entry.getKey())
                                            .append(':')
                                            .append(entry.getValue()));
        }
        return fingerprint(
                value.append('|')
                        .append(quote.usePoint())
                        .append('|')
                        .append(quote.givePoint())
                        .toString());
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

    /** 计算业务请求内容指纹。 */
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

    /** 长度前缀区分空值和包含分隔符的规格/图片，排序后的Map不受迭代顺序影响。 */
    private static void appendSnapshotText(StringBuilder target, String value) {
        target.append('|').append(value == null ? -1 : value.length()).append(':');
        if (value != null) {
            target.append(value);
        }
    }
}

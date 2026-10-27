package com.lemnt.mall.domain.catalog.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Pure rules shared by HTTP, RPC and future import use cases.
 */
public final class CatalogRules {
    private CatalogRules() {}

    /**
     * 文本。
     */
    public static String text(String value, int maximum, String label) {
        if (value == null || value.isBlank() || value.trim().length() > maximum) {
            throw new IllegalArgumentException(label + "不合法");
        }
        return value.trim();
    }

    /**
     * 可选。
     */
    public static String optional(String value, int maximum) {
        if (value != null && value.length() > maximum) {
            throw new IllegalArgumentException("字段过长");
        }
        return value;
    }

    /**
     * 编号。
     */
    public static void id(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("编号不合法");
        }
    }

    /**
     * 状态。
     */
    public static String status(String status) {
        if (!Set.of("ENABLED", "DISABLED").contains(status == null ? "" : status)) {
            throw new IllegalArgumentException("状态只能为 ENABLED 或 DISABLED");
        }
        return status;
    }

    /**
     * 价格。
     */
    public static BigDecimal price(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0 || amount.precision() - amount.scale() > 17) {
            throw new IllegalArgumentException("价格必须大于0且不超过DECIMAL(19,2)");
        }
        try {
            return amount.setScale(2, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException ex) {
            throw new IllegalArgumentException("价格最多两位小数", ex);
        }
    }

    /**
     * 配置属性。
     */
    public static Map<String, String> properties(Map<String, String> properties) {
        if (properties == null) {
            return Map.of();
        }
        if (properties.size() > 10) {
            throw new IllegalArgumentException("最多10项规格");
        }
        var result = new TreeMap<String, String>();
        properties.forEach(
                (key, value) -> {
            String normalized = text(key, 64, "规格名称");
            if (result.put(normalized, text(value, 100, "规格值")) != null) {
                throw new IllegalArgumentException("规格名称重复");
            }
        });
        return Collections.unmodifiableMap(result);
    }

    /**
     * 规格组合键。
     */
    public static String variantKey(Map<String, String> properties) {
        StringBuilder canonical = new StringBuilder();
        new TreeMap<>(properties(properties))
            .forEach(
                (key, value) ->
                canonical
                .append(key.length())
                .append(':')
                .append(key)
                .append(value.length())
                .append(':')
                .append(value));
        try {
            return HexFormat.of()
                .formatHex(
                    MessageDigest.getInstance("SHA-256")
                    .digest(canonical.toString().getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException(impossible);
        }
    }

    /**
     * 新商品SPU。
     */
    public static CatalogSpu newSpu(CatalogSpu input) {
        if (input == null || input.skus().isEmpty() || input.skus().size() > 100) {
            throw new IllegalArgumentException("商品必须包含1至100个SKU");
        }
        id(input.categoryId());
        id(input.brandId());
        Set<String> codes = new HashSet<>();
        Set<String> variants = new HashSet<>();
        Set<String> names = null;
        List<CatalogSku> skus = new ArrayList<>();
        for (CatalogSku sku : input.skus()) {
            if (sku == null || sku.stock() == null || sku.stock() < 0) {
                throw new IllegalArgumentException("SKU库存不合法");
            }
            String code = text(sku.code(), 64, "SKU编码");
            Map<String, String> props = properties(sku.properties());
            if (!codes.add(code) || !variants.add(variantKey(props))) {
                throw new IllegalArgumentException("SKU编码或规格组合重复");
            }
            if (names != null && !names.equals(props.keySet())) {
                throw new IllegalArgumentException("同一商品的SKU必须使用相同的规格名称");
            }
            names = props.keySet();
            skus.add(
                    new CatalogSku(
                    null,
                    null,
                    code,
                    text(sku.name(), 200, "SKU名称"),
                    price(sku.price()),
                    sku.stock(),
                    optional(sku.image(), 500),
                    props,
                    "ON_SHELF"));
        }
        return new CatalogSpu(
                null,
                text(input.code(), 64, "SPU编码"),
                text(input.name(), 200, "商品名称"),
                input.categoryId(),
                input.brandId(),
                optional(input.description(), 65535),
                optional(input.image(), 500),
                "OFF_SHELF",
                skus);
    }
}

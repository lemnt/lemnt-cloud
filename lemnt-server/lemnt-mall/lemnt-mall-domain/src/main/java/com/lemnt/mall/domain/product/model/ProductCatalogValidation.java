package com.lemnt.mall.domain.product.model;

import com.lemnt.common.core.exception.ServiceException;

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
public final class ProductCatalogValidation {
    private ProductCatalogValidation() {}

    /**
     * 文本。
     */
    public static String text(String value, int maximum, String label) {
        if (value == null || value.isBlank() || value.trim().length() > maximum
        ) {
            throw new IllegalArgumentException(label + "不合法");
        }
        return value.trim();
    }

    /**
     * 可选。
     */
    public static String optional(String value, int maximum) {
        if (value != null && value.length() > maximum
        ) {
            throw new IllegalArgumentException("字段过长");
        }
        return value;
    }

    /**
     * 编号。
     */
    public static void id(Long id) {
        if (id == null || id <= 0
        ) {
            throw new IllegalArgumentException("编号不合法");
        }
    }

    /**
     * 状态。
     */
    public static String status(String status) {
        if (!Set.of("ENABLED", "DISABLED").contains(status == null ? "" : status)
        ) {
            throw new IllegalArgumentException("状态只能为 ENABLED 或 DISABLED");
        }
        return status;
    }

    /**
     * 价格。
     */
    public static BigDecimal price(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0 || amount.precision() - amount.scale() > 17
        ) {
            throw new IllegalArgumentException("价格必须大于0且不超过DECIMAL(19,2)");
        }
        try {
            return amount.setScale(2, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException ex
        ) {
            throw new IllegalArgumentException("价格最多两位小数", ex);
        }
    }

    /**
     * 配置属性。
     */
    public static Map<String, String> properties(Map<String, String> properties) {
        if (properties == null
        ) {
            return Map.of();
        }
        if (properties.size() > 10
        ) {
            throw new IllegalArgumentException("最多10项规格");
        }
        var result = new TreeMap<String, String>();
        properties.forEach(
                (key, value) -> {
            String normalized = text(key, 64, "规格名称");
            if (result.put(normalized, text(value, 100, "规格值")) != null
            ) {
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
        } catch (NoSuchAlgorithmException impossible
        ) {
            throw new IllegalStateException(impossible);
        }
    }

    /**
     * 新商品SPU。
     */
    public static ProductSpu newSpu(ProductSpu input) {
        if (input == null || input.skus().isEmpty() || input.skus().size() > 100
        ) {
            throw new IllegalArgumentException("商品必须包含1至100个SKU");
        }
        if (input.deliveryTypes().stream().anyMatch(type -> type == null || (type != 1 && type != 2))
                || input.deliveryTypes().stream().distinct().count() != input.deliveryTypes().size()
                || (input.deliveryTypes().contains(1) && (input.deliveryTemplateId() == null || input.deliveryTemplateId() <= 0))
        ) {
            throw new ServiceException("商品配送方式或运费模板不合法", 400);
        }
        id(input.categoryId());
        id(input.brandId());
        Set<String> codes = new HashSet<>();
        Set<String> variants = new HashSet<>();
        Set<String> names = null;
        List<ProductSku> skus = new ArrayList<>();
        for (ProductSku sku : input.skus()
        ) {
            if (sku == null || sku.stock() == null || sku.stock() < 0
            ) {
                throw new IllegalArgumentException("SKU库存不合法");
            }
            if ((sku.weight() != null && (sku.weight().signum() < 0 || sku.weight().scale() > 6
                        || sku.weight().precision() - sku.weight().scale() > 12))
                    || (sku.volume() != null && (sku.volume().signum() < 0 || sku.volume().scale() > 6
                        || sku.volume().precision() - sku.volume().scale() > 12))
            ) {
                throw new ServiceException("商品重量或体积不合法", 400);
            }
            String code = text(sku.code(), 64, "SKU编码");
            Map<String, String> props = properties(sku.properties());
            if (!codes.add(code) || !variants.add(variantKey(props))
            ) {
                throw new IllegalArgumentException("SKU编码或规格组合重复");
            }
            if (names != null && !names.equals(props.keySet())
            ) {
                throw new IllegalArgumentException("同一商品的SKU必须使用相同的规格名称");
            }
            names = props.keySet();
            skus.add(
                    new ProductSku(
                    sku.id(),
                    sku.spuId(),
                    code,
                    text(sku.name(), 200, "SKU名称"),
                    price(sku.price()),
                    sku.stock(),
                    optional(sku.image(), 500),
                    props,
                    "ON_SHELF", sku.weight(), sku.volume(), sku.specValueIds()));
        }
        return new ProductSpu(
                null,
                text(input.code(), 64, "SPU编码"),
                text(input.name(), 200, "商品名称"),
                input.categoryId(),
                input.brandId(),
                optional(input.description(), 65535),
                optional(input.image(), 500),
                "OFF_SHELF",
                skus, input.deliveryTypes(), input.deliveryTemplateId());
    }

    /** 校验批量商品标识，拒绝重复编号和超量请求。 */
    public static List<Long> ids(List<Long> ids, int maximum) {
        if (ids == null || ids.isEmpty() || ids.size() > maximum
                || ids.stream().anyMatch(id -> id == null || id <= 0)
                || new HashSet<>(ids).size() != ids.size()
        ) {
            throw new IllegalArgumentException("商品编号集合不合法");
        }
        return ids.stream().sorted().toList();
    }
}

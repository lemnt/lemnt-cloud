package com.lemnt.mall.domain.product.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/** 商品SKU及原规格行，编号和名称一起传递。 */
public record ProductSku(Long id, Long spuId, String code, String name, BigDecimal price, Integer stock,
        String image, List<ProductSkuSpec> properties, String status, BigDecimal weight, BigDecimal volume) {
    public ProductSku {
        properties = properties == null ? List.of() : List.copyOf(properties);
    }

    /** 存量名值转换不推断字典编号。 */
    public ProductSku(Long id, Long spuId, String code, String name, BigDecimal price, Integer stock,
            String image, Map<String, String> properties, String status, BigDecimal weight, BigDecimal volume) {
        this(id, spuId, code, name, price, stock, image, legacy(properties), status, weight, volume);
    }

    /** 兼容缺少配送参数的存量名值调用。 */
    public ProductSku(Long id, Long spuId, String code, String name, BigDecimal price, Integer stock,
            String image, Map<String, String> properties, String status) {
        this(id, spuId, code, name, price, stock, image, properties, status, null, null);
    }

    /** 兼容尚未配置重量、体积的规格列表。 */
    public ProductSku(Long id, Long spuId, String code, String name, BigDecimal price, Integer stock,
            String image, List<ProductSkuSpec> properties, String status) {
        this(id, spuId, code, name, price, stock, image, properties, status, null, null);
    }

    private static List<ProductSkuSpec> legacy(Map<String, String> values) {
        return values == null ? List.of() : values.entrySet().stream().sorted(Map.Entry.comparingByKey())
                .map(entry -> new ProductSkuSpec(null, entry.getKey(), null, entry.getValue())).toList();
    }
}

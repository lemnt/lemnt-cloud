package com.lemnt.promotion.domain.product.model;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * SKU规格字典关联及保存时的名称快照。
 */
public record ProductSkuSpec(Long specId, String specName, Long specValueId, String specValueName) {
    /**
     * 名值读取不依赖当前字典。
     */
    public static Map<String, String> names(List<ProductSkuSpec> values) {
        var result = new TreeMap<String, String>();
        values.forEach(value -> result.put(value.specName(), value.specValueName()));
        return result;
    }
}

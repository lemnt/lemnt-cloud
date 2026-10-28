package com.lemnt.mall.domain.product.model;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** SKU规格字典关联及保存时的名称快照。 */
public record ProductSkuSpec(Long propertyId, String propertyName, Long valueId, String valueName) {
    /** 名值读取不依赖当前字典。 */
    public static Map<String, String> names(List<ProductSkuSpec> values) {
        var result = new TreeMap<String, String>();
        values.forEach(value -> result.put(value.propertyName(), value.valueName()));
        return result;
    }

    /** 任一旧行缺少编号时整组未知，不通过名称猜测。 */
    public static Map<Long, Long> identities(List<ProductSkuSpec> values) {
        if (values.stream().anyMatch(value -> value.propertyId() == null || value.valueId() == null)) {
            return null;
        }
        var result = new TreeMap<Long, Long>();
        values.forEach(value -> result.put(value.propertyId(), value.valueId()));
        return result;
    }
}

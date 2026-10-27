package com.lemnt.mall.infrastructure.catalog.gateway;

import com.lemnt.mall.domain.catalog.gateway.SkuPropertyGateway;
import com.lemnt.mall.infrastructure.catalog.dataobject.SkuPropertyDO;
import com.lemnt.mall.infrastructure.catalog.mapper.SkuPropertyMapper;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/** SkuProperty 表持久化实现。 */
@Component
@RequiredArgsConstructor
public class SkuPropertyGatewayImpl implements SkuPropertyGateway {
    private final SkuPropertyMapper skuPropertyMapper;

    /** 读取单个 SKU 的属性。 */
    @Override
    public Map<String, String> properties(Long id) {
        var values = new TreeMap<String, String>();
        skuPropertyMapper.properties(id).forEach(row -> values.put(row.getPropertyName(), row.getPropertyValue()));
        return values;
    }

    /** 批量读取商品所有 SKU 的属性。 */
    @Override
    public Map<Long, Map<String, String>> propertiesBySpu(Long id) {
        var values = new HashMap<Long, Map<String, String>>();
        skuPropertyMapper.propertiesBySpu(id).forEach(row -> values.computeIfAbsent(row.getProductId(),
                key -> new TreeMap<>()).put(row.getPropertyName(),
                row.getPropertyValue()));
        return values;
    }

    /** 保存 SKU 的属性集合。 */
    @Override
    public void saveProperties(Long id, Map<String, String> values) {
        values.forEach((name, value) -> {
                var row = new SkuPropertyDO();
                row.setProductId(id);
                row.setPropertyName(name);
                row.setPropertyValue(value);
                skuPropertyMapper.insertProperty(row);
                });
    }
}

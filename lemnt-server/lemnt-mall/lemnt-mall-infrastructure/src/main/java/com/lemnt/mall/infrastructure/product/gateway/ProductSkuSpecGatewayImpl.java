package com.lemnt.mall.infrastructure.product.gateway;

import com.lemnt.mall.domain.product.gateway.ProductSkuSpecGateway;
import com.lemnt.mall.infrastructure.product.dataobject.ProductSkuSpecDO;
import com.lemnt.mall.infrastructure.product.mapper.ProductSkuSpecMapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** mall_product_sku_spec 表（SKU规格快照）持久化实现。 */
@Component
@RequiredArgsConstructor
public class ProductSkuSpecGatewayImpl implements ProductSkuSpecGateway {
    private final ProductSkuSpecMapper productSkuSpecMapper;

    @Override
    public Map<Long, Map<String, String>> propertiesBySkuIds(List<Long> ids) {
        var result = new HashMap<Long, Map<String, String>>();
        if (ids.isEmpty()) {
            return result;
        }
        productSkuSpecMapper.selectList(Wrappers.lambdaQuery(ProductSkuSpecDO.class)
                .in(ProductSkuSpecDO::getProductId, ids)).forEach(row -> result
                        .computeIfAbsent(row.getProductId(), key -> new TreeMap<>())
                        .put(row.getPropertyName(), row.getPropertyValue()));
        return result;
    }

    @Override
    public boolean referenced(String name, String value) {
        return productSkuSpecMapper.selectCount(Wrappers.lambdaQuery(ProductSkuSpecDO.class)
                .eq(ProductSkuSpecDO::getPropertyName, name).eq(value != null, ProductSkuSpecDO::getPropertyValue,
                    value)) > 0;
    }

    /** 读取单个 SKU 的属性。 */
    @Override
    public Map<String, String> properties(Long id) {
        var values = new TreeMap<String, String>();
        productSkuSpecMapper.selectList(Wrappers.<ProductSkuSpecDO>lambdaQuery()
                .eq(ProductSkuSpecDO::getProductId, id)
                .orderByAsc(ProductSkuSpecDO::getPropertyName))
                .forEach(row -> values.put(row.getPropertyName(), row.getPropertyValue()));
        return values;
    }

    /** 批量读取商品所有 SKU 的属性。 */
    @Override
    public Map<Long, Map<String, String>> propertiesBySpu(Long id) {
        var values = new HashMap<Long, Map<String, String>>();
        productSkuSpecMapper.propertiesBySpu(id).forEach(row -> values.computeIfAbsent(row.getProductId(),
                key -> new TreeMap<>()).put(row.getPropertyName(),
                row.getPropertyValue()));
        return values;
    }

    /** 保存 SKU 的属性集合。 */
    @Override
    public void saveProperties(Long id, Map<String, String> values) {
        productSkuSpecMapper.delete(Wrappers.<ProductSkuSpecDO>lambdaQuery()
                .eq(ProductSkuSpecDO::getProductId, id));
        values.forEach((name, value) -> {
                var row = new ProductSkuSpecDO();
                row.setProductId(id);
                row.setPropertyName(name);
                row.setPropertyValue(value);
                productSkuSpecMapper.insert(row);
                });
    }
}

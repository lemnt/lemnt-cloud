package com.lemnt.mall.infrastructure.product.gateway;

import com.lemnt.mall.domain.product.gateway.ProductSkuSpecGateway;
import com.lemnt.mall.domain.product.model.ProductSkuSpec;
import com.lemnt.mall.infrastructure.product.dataobject.ProductSkuSpecDO;
import com.lemnt.mall.infrastructure.product.mapper.ProductSkuSpecMapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** mall_product_sku_spec表持久化，编号与名称一次读取。 */
@Component
@RequiredArgsConstructor
public class ProductSkuSpecGatewayImpl implements ProductSkuSpecGateway {
    private final ProductSkuSpecMapper productSkuSpecMapper;

    @Override
    public Map<Long, List<ProductSkuSpec>> specsBySkuIds(List<Long> ids) {
        return ids.isEmpty() ? Map.of() : grouped(productSkuSpecMapper.selectList(
                Wrappers.lambdaQuery(ProductSkuSpecDO.class).in(ProductSkuSpecDO::getProductId, ids)));
    }

    @Override
    public Map<Long, List<ProductSkuSpec>> specsBySpu(Long id) {
        return grouped(productSkuSpecMapper.propertiesBySpu(id));
    }

    private Map<Long, List<ProductSkuSpec>> grouped(List<ProductSkuSpecDO> rows) {
        return rows.stream().collect(Collectors.groupingBy(ProductSkuSpecDO::getProductId,
                Collectors.mapping(row -> new ProductSkuSpec(row.getPropertyId(), row.getPropertyName(),
                        row.getValueId(), row.getPropertyValue()), Collectors.toList())));
    }

    @Override
    public void saveProperties(Long id, Map<String, String> values) {
        saveSpecs(id, values.entrySet().stream().map(entry ->
                new ProductSkuSpec(null, entry.getKey(), null, entry.getValue())).toList());
    }

    @Override
    public void saveSpecs(Long id, List<ProductSkuSpec> values) {
        productSkuSpecMapper.delete(Wrappers.lambdaQuery(ProductSkuSpecDO.class).eq(ProductSkuSpecDO::getProductId, id));
        values.forEach(value -> productSkuSpecMapper.insert(new ProductSkuSpecDO().setProductId(id)
                .setPropertyId(value.propertyId()).setValueId(value.valueId())
                .setPropertyName(value.propertyName()).setPropertyValue(value.valueName())));
    }

    @Override
    public boolean referenced(Long propertyId, Long valueId, String name, String value) {
        return productSkuSpecMapper.selectCount(Wrappers.lambdaQuery(ProductSkuSpecDO.class)
                .and(query -> query.eq(ProductSkuSpecDO::getPropertyId, propertyId)
                        .eq(valueId != null, ProductSkuSpecDO::getValueId, valueId)
                        .or(legacy -> legacy.isNull(ProductSkuSpecDO::getPropertyId)
                                .eq(ProductSkuSpecDO::getPropertyName, name)
                                .eq(value != null, ProductSkuSpecDO::getPropertyValue, value)))) > 0;
    }
}

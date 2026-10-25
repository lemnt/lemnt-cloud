package com.lemnt.mall.infrastructure.catalog.gateway;

import com.lemnt.mall.domain.catalog.gateway.CatalogGateway;
import com.lemnt.mall.domain.catalog.model.*;
import com.lemnt.mall.infrastructure.catalog.converter.CatalogConverter;
import com.lemnt.mall.infrastructure.catalog.dataobject.*;
import com.lemnt.mall.infrastructure.catalog.mapper.CatalogMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CatalogGatewayImpl implements CatalogGateway {
    private final CatalogMapper mapper;

    @Override
    public List<Category> categories() {
        var rows = mapper.categories();
        if (rows.size() > 1000) throw new IllegalStateException("分类超过1000条，请先接入分页分类维护");
        return rows.stream().map(CatalogConverter::category).toList();
    }

    @Override
    public Category category(Long id) {
        return CatalogConverter.category(mapper.category(id));
    }

    @Override
    public Category saveCategory(Category value) {
        var row = CatalogConverter.category(value);
        if (value.id() == null) mapper.insertCategory(row);
        else if (mapper.updateCategory(row) != 1) throw new IllegalArgumentException("分类不存在");
        return CatalogConverter.category(row);
    }

    @Override
    public boolean deleteCategory(Long id) {
        try {
            return mapper.deleteCategory(id) == 1;
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalStateException("分类仍被子分类或商品引用", ex);
        }
    }

    @Override
    public List<Brand> brands() {
        var rows = mapper.brands();
        if (rows.size() > 1000) throw new IllegalStateException("品牌超过1000条，请先接入分页品牌维护");
        return rows.stream().map(CatalogConverter::brand).toList();
    }

    @Override
    public Brand brand(Long id) {
        return CatalogConverter.brand(mapper.brand(id));
    }

    @Override
    public Brand saveBrand(Brand value) {
        var row = CatalogConverter.brand(value);
        if (value.id() == null) mapper.insertBrand(row);
        else if (mapper.updateBrand(row) != 1) throw new IllegalArgumentException("品牌不存在");
        return CatalogConverter.brand(row);
    }

    @Override
    public boolean deleteBrand(Long id) {
        try {
            return mapper.deleteBrand(id) == 1;
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalStateException("品牌仍被商品引用", ex);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CatalogSpu createSpu(CatalogSpu value) {
        var row = CatalogConverter.spu(value);
        mapper.insertSpu(row);
        for (var sku : value.skus()) {
            var child = CatalogConverter.sku(sku);
            child.setSpuId(row.getId());
            mapper.insertSku(child);
            sku.properties()
                    .forEach(
                            (name, propertyValue) -> {
                                var property = new SkuPropertyDO();
                                property.setProductId(child.getId());
                                property.setPropertyName(name);
                                property.setPropertyValue(propertyValue);
                                mapper.insertProperty(property);
                            });
        }
        return spu(row.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public CatalogSpu spu(Long id) {
        var row = mapper.spu(id);
        if (row == null) return null;
        var properties =
                mapper.propertiesBySpu(id).stream()
                        .collect(Collectors.groupingBy(SkuPropertyDO::getProductId));
        var skus =
                mapper.skus(id).stream()
                        .map(
                                sku ->
                                        CatalogConverter.sku(
                                                sku,
                                                properties.getOrDefault(sku.getId(), List.of())))
                        .toList();
        return CatalogConverter.spu(row, skus);
    }

    @Override
    @Transactional(readOnly = true)
    public CatalogPage page(CatalogFilter filter) {
        return new CatalogPage(
                mapper.page(filter).stream()
                        .map(row -> CatalogConverter.spu(row, List.of()))
                        .toList(),
                mapper.count(filter));
    }

    @Override
    public boolean updateSpu(CatalogSpu value) {
        return mapper.updateSpu(CatalogConverter.spu(value)) == 1;
    }

    @Override
    public boolean shelf(Long id, boolean onShelf) {
        return mapper.shelf(id, onShelf ? "ON_SHELF" : "OFF_SHELF") == 1;
    }

    @Override
    public CatalogSku sku(Long id) {
        return CatalogConverter.sku(mapper.sku(id), mapper.properties(id));
    }

    @Override
    public boolean updateSku(CatalogSku value) {
        return mapper.updateSku(CatalogConverter.sku(value)) == 1;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StockAdjustment adjustStock(StockAdjustment value) {
        var previous = mapper.adjustment(value.requestNo());
        if (previous != null) {
            if (!previous.getProductId().equals(value.productId())
                    || previous.getDelta() != value.delta()
                    || !previous.getReason().equals(value.reason())) {
                throw new IllegalStateException("库存请求编号已用于不同的操作");
            }
            return CatalogConverter.adjustment(previous);
        }
        var row = new StockAdjustmentDO();
        row.setRequestNo(value.requestNo());
        row.setProductId(value.productId());
        row.setDelta(value.delta());
        row.setReason(value.reason());
        // Unique request key is acquired before the stock update; duplicate insert rolls back.
        mapper.insertAdjustment(row);
        if (mapper.adjustStock(row) != 1) throw new IllegalStateException("商品不存在或库存调整越界");
        row.setResultingStock(mapper.sku(value.productId()).getStock());
        if (mapper.finishAdjustment(row) != 1) throw new IllegalStateException("库存台账保存失败");
        return CatalogConverter.adjustment(row);
    }
}

package com.lemnt.product.infrastructure.product.gateway;

import com.lemnt.product.domain.product.gateway.ProductCategoryGateway;
import com.lemnt.product.domain.product.model.ProductCategory;
import com.lemnt.product.infrastructure.product.converter.ProductCatalogConverter;
import com.lemnt.product.infrastructure.product.dataobject.ProductCategoryDO;
import com.lemnt.product.infrastructure.product.mapper.ProductCategoryMapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * ProductCategory 表持久化实现。
 */
@Component
@RequiredArgsConstructor
public class ProductCategoryGatewayImpl implements ProductCategoryGateway {
    private final ProductCategoryMapper categoryMapper;

    @Override
    public List<ProductCategory> categories() {
        var rows =
            categoryMapper.selectList(
                Wrappers.<ProductCategoryDO>lambdaQuery()
                    .orderByAsc(ProductCategoryDO::getSort)
                    .orderByAsc(ProductCategoryDO::getId)
                    .last("LIMIT 1001"));
        if (rows.size() > 1000) {
            throw new IllegalStateException("分类超过1000条，请先接入分页分类维护");
        }
        return rows.stream().map(ProductCatalogConverter::category).toList();
    }

    @Override
    public ProductCategory category(Long id) {
        return ProductCatalogConverter.category(categoryMapper.selectById(id));
    }

    @Override
    public ProductCategory saveCategory(ProductCategory value) {
        var row = ProductCatalogConverter.category(value);
        if (value.id() == null) {
            categoryMapper.insert(row);
        } else if (categoryMapper.update(
            null,
            Wrappers.<ProductCategoryDO>lambdaUpdate()
                .set(ProductCategoryDO::getName, row.getName())
                .set(ProductCategoryDO::getPicUrl, row.getPicUrl())
                .set(ProductCategoryDO::getSort, row.getSort())
                .set(ProductCategoryDO::getStatus, row.getStatus())
                .eq(ProductCategoryDO::getId, row.getId()))
            != 1) {
            throw new IllegalArgumentException("分类不存在");
        }
        return ProductCatalogConverter.category(row);
    }

    @Override
    public boolean deleteCategory(Long id) {
        try {
            return categoryMapper.deleteById(id) == 1;
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalStateException("分类仍被子分类或商品引用", ex);
        }
    }

    @Override
    /**
     * 查询分类。
     */
    public List<ProductCategory> visibleCategories() {
        var rows = categoryMapper.storeCatalogCategories();
        if (rows.size() > 1000) {
            throw new IllegalStateException("分类超过1000条，需要分页");
        }
        return rows.stream()
            .map(
                v ->
                    new ProductCategory(
                        v.getId(),
                        v.getParentId(),
                        v.getName(),
                        v.getPicUrl(),
                        v.getSort(),
                        v.getStatus()))
            .toList();
    }
}

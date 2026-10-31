package com.lemnt.mall.infrastructure.product.gateway;

import com.lemnt.mall.domain.product.gateway.CategoryGateway;
import com.lemnt.mall.domain.product.model.Category;
import com.lemnt.mall.infrastructure.product.converter.ProductCatalogConverter;
import com.lemnt.mall.infrastructure.product.dataobject.CategoryDO;
import com.lemnt.mall.infrastructure.product.mapper.CategoryMapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

/** Category 表持久化实现。 */
@Component
@RequiredArgsConstructor
public class CategoryGatewayImpl implements CategoryGateway {
    private final CategoryMapper categoryMapper;

    @Override
    public List<Category> categories() {
        var rows = categoryMapper.selectList(Wrappers.<CategoryDO>lambdaQuery()
                .orderByAsc(CategoryDO::getSort)
                .orderByAsc(CategoryDO::getId)
                .last("LIMIT 1001"));
        if (rows.size() > 1000) {
            throw new IllegalStateException("分类超过1000条，请先接入分页分类维护");
        }
        return rows.stream().map(ProductCatalogConverter::category).toList();
    }

    @Override
    public Category category(Long id) {
        return ProductCatalogConverter.category(categoryMapper.selectById(id));
    }

    @Override
    public Category saveCategory(Category value) {
        var row = ProductCatalogConverter.category(value);
        if (value.id() == null) {
            categoryMapper.insert(row);
        }
        else if (categoryMapper.update(null, Wrappers.<CategoryDO>lambdaUpdate()
                .set(CategoryDO::getName, row.getName())
                .set(CategoryDO::getPicUrl, row.getPicUrl())
                .set(CategoryDO::getSort, row.getSort())
                .set(CategoryDO::getStatus, row.getStatus())
                .eq(CategoryDO::getId, row.getId())) != 1) {
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
    public List<Category> visibleCategories() {
        var rows = categoryMapper.storeCatalogCategories();
        if (rows.size() > 1000) {
            throw new IllegalStateException("分类超过1000条，需要分页");
        }
        return rows.stream()
                .map(
                v ->
                new Category(
                v.getId(),
                v.getParentId(),
                v.getName(),
                v.getPicUrl(),
                v.getSort(),
                v.getStatus()))
                .toList();
    }
}

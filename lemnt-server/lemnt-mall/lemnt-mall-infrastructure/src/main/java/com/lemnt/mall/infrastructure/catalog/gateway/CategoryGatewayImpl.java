package com.lemnt.mall.infrastructure.catalog.gateway;

import com.lemnt.mall.domain.catalog.gateway.CategoryGateway;
import com.lemnt.mall.domain.catalog.model.Category;
import com.lemnt.mall.infrastructure.catalog.converter.CatalogConverter;
import com.lemnt.mall.infrastructure.catalog.mapper.CategoryMapper;

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
        var rows = categoryMapper.catalogCategories();
        if (rows.size() > 1000) {
            throw new IllegalStateException("分类超过1000条，请先接入分页分类维护");
        }
        return rows.stream().map(CatalogConverter::category).toList();
    }

    @Override
    public Category category(Long id) {
        return CatalogConverter.category(categoryMapper.category(id));
    }

    @Override
    public Category saveCategory(Category value) {
        var row = CatalogConverter.category(value);
        if (value.id() == null) {
            categoryMapper.insertCategory(row);
        }
        else if (categoryMapper.updateCategory(row) != 1) {
            throw new IllegalArgumentException("分类不存在");
        }
        return CatalogConverter.category(row);
    }

    @Override
    public boolean deleteCategory(Long id) {
        try {
            return categoryMapper.deleteCategory(id) == 1;
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

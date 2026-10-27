package com.lemnt.mall.application.catalog.service;

import com.lemnt.mall.domain.catalog.gateway.BrandGateway;
import com.lemnt.mall.domain.catalog.gateway.CategoryGateway;
import com.lemnt.mall.domain.catalog.model.CatalogRules;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 目录校验服务。
 */
@Component
@RequiredArgsConstructor
public class CatalogValidationService {
    private final BrandGateway brandGateway;
    private final CategoryGateway categoryGateway;

    /**
     * 已启用引用。
     */
    public void enabledReferences(Long categoryId, Long brandId) {
        CatalogRules.id(categoryId);
        CatalogRules.id(brandId);
        var category = categoryGateway.category(categoryId);
        var brand = brandGateway.brand(brandId);
        if (category == null
                || !"ENABLED".equals(category.status())
                || brand == null
                || !"ENABLED".equals(brand.status())) {
            throw new IllegalArgumentException("分类或品牌不存在或已停用");
        }
        if (category.parentId() != null) {
            var parent = categoryGateway.category(category.parentId());
            if (parent == null || !"ENABLED".equals(parent.status())) {
                throw new IllegalArgumentException("父分类已停用");
            }
        }
    }
}
